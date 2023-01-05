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

    private String grantType;
    private String scope;

    public ScheduledAccessTokenPublisher(String uri, String apimKey, String username, String password, Clock clock, ScheduledExecutorService scheduler, HttpClient httpClient) {
        this.uri = uri;
        this.headers = createHeaders(apimKey, username, password, false);
        this.clock = clock;
        this.scheduler = scheduler;
        this.httpClient = httpClient;
        scheduleFetch(0);
    }

    public ScheduledAccessTokenPublisher(String uri, String id, String secret, String scope, String grantType, HttpClient httpClient) {
        this.uri = uri;
        this.headers = createHeaders(null, id, secret, scope != null || grantType != null);
        this.clock = Clock.systemDefaultZone();
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.httpClient = httpClient;
        this.scope = scope;
        this.grantType = grantType;
        scheduleFetch(0);
    }

    private static LinkedHashMap<String, List<String>> createHeaders(String apimKey, String id, String secret, boolean body) {
        var headers = new LinkedHashMap<String, List<String>>();
        if (apimKey != null) {
            headers.put("Ocp-Apim-Subscription-Key", List.of(apimKey));
        }
        if (body) {
            headers.put("Content-type", List.of("application/x-www-form-urlencoded"));
        }
        headers.put("Authorization", List.of("Basic " + Base64.getEncoder().encodeToString((id + ":" + secret).getBytes(StandardCharsets.UTF_8))));
        return headers;
    }

    private void scheduleFetch(long millis) {
        if (shutDown) return;
        scheduler.schedule(this::fetchNewToken, millis, TimeUnit.MILLISECONDS);
    }

    private void fetchNewToken() {
        if (shutDown) return;
        String body = createBody();
        httpClient.post(uri, new SinglePublisher<>(body, fetchExecutor), headers).subscribe(this);
    }

    private String createBody() {
        if (scope == null && grantType == null) return "";
        return String.format("grant_type=%s&scopes=%s",
                grantType,
                scope
        );
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
            token = AccessToken.parse(item.getBody());
        } catch (Exception e) {
            onError(e);
            return;
        }
        atomicToken.set(token);
        synchronized (subscribers) {
            subscribers.forEach(subscriber -> subscriber.onNext(token.getToken()));
            subscribers.clear();
        }
        scheduleFetch(token.tenSecondsBeforeExpiry(clock));
    }

    @Override
    public void onError(Throwable throwable) {
        scheduleFetch(5 * 1000);
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
        if (token != null){
            if(!token.getExpiry().isBefore(clock.instant())) {
                var exception = new IllegalStateException("Token is expired at: " + token.getExpiry().toString());
                subscriber.onError(exception);
                onError(exception);

            }
            subscriber.onNext(token.getToken());
        }
        else {
            synchronized (subscribers) {
                subscribers.add(subscriber);
            }
        }
    }
}
