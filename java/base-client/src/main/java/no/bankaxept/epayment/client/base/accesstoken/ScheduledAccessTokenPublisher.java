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
import java.util.stream.Collectors;

public class ScheduledAccessTokenPublisher implements AccessTokenPublisher, Flow.Subscriber<HttpResponse> {
    private final ScheduledExecutorService scheduler;
    private final ExecutorService fetchExecutor = Executors.newSingleThreadExecutor();
    private final Clock clock;

    private boolean shutDown;

    private final HttpClient httpClient;
    private final String uri;
    private final LinkedHashMap<String, List<String>> headers;
    private final String body;

    private final AtomicReference<AccessToken> atomicToken = new AtomicReference<>();

    private final Queue<Flow.Subscriber<? super String>> subscribers = new LinkedBlockingQueue<>();

    private ScheduledAccessTokenPublisher(String uri, LinkedHashMap<String, List<String>> headers, String body, Clock clock, ScheduledExecutorService scheduler, HttpClient httpClient) {
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
            onError(new HttpStatusException(item.getStatus(), "Error when fetching token:"));
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
        if (token != null){
            if(token.getExpiry().isBefore(clock.instant())) {
                var exception = new IllegalStateException("Token already expired at: " + token.getExpiry());
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

    public static class Builder {
        private String uri;
        private HttpClient httpClient;
        private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        private Clock clock = Clock.systemDefaultZone();

        private final LinkedHashMap<String, List<String>> headers = new LinkedHashMap<>();
        private final StringBuilder body = new StringBuilder();

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

        public Builder credentials(String id, String secret) {
            headers.put("Authorization", List.of("Basic " + Base64.getEncoder().encodeToString((id + ":" + secret).getBytes(StandardCharsets.UTF_8))));
            return this;
        }

        public Builder grantType(GrantType grantType) {
            prefix().append("grant_type=").append(grantType);
            return this;
        }

        public Builder scopes(List<Scope> scopes) {
            prefix().append("scopes=").append(scopes.stream().map(Scope::getValue).collect(Collectors.joining(",")));
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

        private StringBuilder prefix(){
            return body.length() > 0 ? body.append("&") : body;
        }

        public ScheduledAccessTokenPublisher build() {
            if(body.length() > 0) {
                headers.put("Content-type", List.of("application/x-www-form-urlencoded"));
            }
            return new ScheduledAccessTokenPublisher(uri, headers, body.toString(), clock, scheduler, httpClient);
        }


    }
}
