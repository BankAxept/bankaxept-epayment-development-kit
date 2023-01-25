package no.bankaxept.epayment.client.base.accesstoken;

import no.bankaxept.epayment.client.base.SinglePublisher;
import no.bankaxept.epayment.client.base.http.HttpClient;
import no.bankaxept.epayment.client.base.http.HttpResponse;
import no.bankaxept.epayment.client.base.http.HttpStatusException;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class ScheduledAccessTokenPublisher implements AccessTokenPublisher, Flow.Subscriber<HttpResponse> {
    private final ScheduledExecutorService scheduler;
    private final ExecutorService fetchExecutor = Executors.newSingleThreadExecutor();
    private final Clock clock;

    private boolean shutDown;

    private final HttpClient httpClient;
    private final String uri;
    private final LinkedHashMap<String, List<String>> headers;

    private final AtomicReference<AccessToken> atomicToken = new AtomicReference<>();

    private final Queue<Flow.Subscriber<? super String>> subscribers = new LinkedBlockingQueue<>();

    public ScheduledAccessTokenPublisher(String uri, String apimKey, String username, String password, Clock clock, ScheduledExecutorService scheduler, HttpClient httpClient) {
        this.uri = uri;
        this.headers = createHeaders(apimKey, username, password);
        this.clock = clock;
        this.scheduler = scheduler;
        this.httpClient = httpClient;
        scheduleFetch(0);
    }

    private static LinkedHashMap<String, List<String>> createHeaders(String apimKey, String username, String password) {
        var headers = new LinkedHashMap<String, List<String>>();
        headers.put("Ocp-Apim-Subscription-Key", List.of(apimKey));
        headers.put("Authorization", List.of("Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8))));
        return headers;
    }

    private void scheduleFetch(long seconds) {
        if (shutDown) return;
        scheduler.schedule(this::fetchNewToken, seconds, TimeUnit.SECONDS);
    }

    private void fetchNewToken() {
        if (shutDown) return;
        httpClient.post(uri, new SinglePublisher<>("", fetchExecutor), headers).subscribe(this);
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(1);
    }

    @Override
    public void onNext(HttpResponse item) {
        if (!item.getStatus().is2xxOk()) {
            onError(new HttpStatusException(item.getStatus(), "Error when fetching token"));
            return;
        }
        AccessToken token;
        try {
            token = AccessToken.parse(item.getBody(), clock);
        } catch (Exception e) {
            onError(e);
            return;
        }
        atomicToken.set(token);
        synchronized (subscribers) {
            subscribers.forEach(subscriber -> subscriber.onNext(token.getToken()));
            subscribers.clear();
        }
        scheduleFetch(token.secondsUntilTenMinutesBeforeExpiry(clock));
    }

    @Override
    public void onError(Throwable throwable) {
        scheduleFetch(5);
        synchronized (subscribers) {
            subscribers.forEach(subscriber -> subscriber.onError(throwable));
            subscribers.clear();
        }
    }

    @Override
    public void onComplete() {
    }

    public void shutDown() {
        shutDown = true;
        scheduler.shutdown();
        fetchExecutor.shutdown();
        try {
            while (!scheduler.awaitTermination(500, TimeUnit.MILLISECONDS) && !fetchExecutor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
            }
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public void subscribe(Flow.Subscriber<? super String> subscriber) {
        var token = atomicToken.get();
        if (token != null && token.getExpiry().isBefore(clock.instant()))
            subscriber.onNext(token.getToken());
        else {
            synchronized (subscribers) {
                subscribers.add(subscriber);
            }
        }
    }
}
