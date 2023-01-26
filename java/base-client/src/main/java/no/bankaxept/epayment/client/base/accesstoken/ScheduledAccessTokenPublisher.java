package no.bankaxept.epayment.client.base.accesstoken;

import no.bankaxept.epayment.client.base.SinglePublisher;
import no.bankaxept.epayment.client.base.http.HttpClient;
import no.bankaxept.epayment.client.base.http.HttpResponse;
import no.bankaxept.epayment.client.base.http.HttpStatusException;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final Map<String, List<String>> headers;
    private final String body;

    private final AtomicReference<AccessToken> atomicToken = new AtomicReference<>();

    private final Queue<Flow.Subscriber<? super String>> subscribers = new LinkedBlockingQueue<>();

    private ScheduledAccessTokenPublisher(String uri, Map<String, List<String>> headers, String body, Clock clock, ScheduledExecutorService scheduler, HttpClient httpClient) {
        this.uri = uri;
        this.headers = headers;
        this.body = body;
        this.clock = clock;
        this.scheduler = scheduler;
        this.httpClient = httpClient;
        scheduleFetch(0);
    }


    private void scheduleFetch(long seconds) {
        if (shutDown) return;
        scheduler.schedule(this::fetchNewToken, seconds, TimeUnit.SECONDS);
    }

    private void fetchNewToken() {
        if (shutDown) return;
        httpClient.post(uri, new SinglePublisher<>(body, fetchExecutor), headers).subscribe(this);
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
        scheduleFetch(token.secondsUntilTenSecondsBeforeExpiry(clock));
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

    public static class Builder {
        private String uri;
        private HttpClient httpClient;
        private ScheduledExecutorService scheduler;
        private Clock clock = Clock.systemDefaultZone();

        private final Map<String, List<String>> headers = new HashMap<>(Map.of("Content-type", List.of("application/x-www-form-urlencoded")));
        private GrantType grantType;
        private List<String> scopes = new ArrayList<>();

        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public Builder uri(String uri) {
            this.uri = uri;
            return this;
        }

        public Builder apimKey(String apimKey) {
            headers.put("Ocp-Apim-Subscription-Key", List.of(apimKey));
            return this;
        }

        public Builder clientCredentials(String id, String secret) {
            headers.put("Authorization", List.of("Basic " + Base64.getEncoder().encodeToString((id + ":" + secret).getBytes(StandardCharsets.UTF_8))));
            return grantType(GrantType.client_credentials);
        }

        private Builder grantType(GrantType grantType) {
            this.grantType = grantType;
            return this;
        }

        public Builder scopes(List<String> scopes) {
            this.scopes.addAll(scopes);
            return this;
        }

        public Builder clock(Clock clock) {
            this.clock = clock;
            return this;
        }

        public Builder scheduler(ScheduledExecutorService scheduler) {
            this.scheduler = scheduler;
            return this;
        }

        public ScheduledAccessTokenPublisher build() {
            if (grantType == null) {
                throw new IllegalArgumentException("Grant type is not set");
            }
            return new ScheduledAccessTokenPublisher(uri, headers, createBody(), clock, getSchedulerOrDefault(), httpClient);
        }

        private ScheduledExecutorService getSchedulerOrDefault() {
            return scheduler == null ? Executors.newScheduledThreadPool(1) : scheduler;
        }

        private String createBody() {
            var body = new StringBuilder("grant_type=").append(grantType);
            if (!scopes.isEmpty()) {
                body.append("&").append("scopes=").append(String.join(",", scopes));
            }
            return body.toString();
        }
    }
}
