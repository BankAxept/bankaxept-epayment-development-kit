package no.bankaxept.epayment.client.base.accesstoken;

import no.bankaxept.epayment.client.base.http.HttpClient;
import no.bankaxept.epayment.client.base.http.HttpResponse;
import no.bankaxept.epayment.client.base.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class AccessTokenSupplier implements Flow.Subscriber<HttpResponse>, Supplier<String> {
    private final ScheduledExecutorService scheduler;
    private final Clock clock;
    private CountDownLatch startUpLatch = new CountDownLatch(1);

    private HttpClient httpClient;
    private final String uri;
    private final LinkedHashMap<String, List<String>> headers;

    private AtomicReference<AccessToken> atomicToken = new AtomicReference<>();

    public AccessTokenSupplier(String uri, String apimKey, String username, String password, Clock clock, ScheduledExecutorService scheduler, HttpClient httpClient) {
        this.uri = uri;
        this.headers = createHeaders(apimKey, username, password);
        this.clock = clock;
        this.scheduler = scheduler;
        this.httpClient = httpClient;
        scheduleFetchInMillis(0);
    }

    private static LinkedHashMap<String, List<String>> createHeaders(String apimKey, String username, String password) {
        var headers = new LinkedHashMap<String, List<String>>();
        headers.put("Ocp-Apim-Subscription-Key", List.of(apimKey));
        headers.put("Authorization", List.of("Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8))));
        return headers;
    }

    private void scheduleFetchInMillis(long millis) {
        scheduler.schedule(this::fetchNewToken, millis, TimeUnit.MILLISECONDS);
    }

    private void fetchNewToken() {
        var emptyPublisher = new SubmissionPublisher<String>();
        httpClient.post(uri, emptyPublisher, headers).subscribe(this);
        emptyPublisher.submit("");
        emptyPublisher.close();
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(HttpResponse item) {
        try {
            if (!item.getStatus().is2xxOk()) {
                throw new HttpStatusException(item.getStatus());
            }
            atomicToken.set(AccessToken.parse(item.getBody()));
            scheduleFetchInMillis(atomicToken.get().millisUntilTenMinutesBeforeExpiry(clock));
        } catch (Exception e) {
            onError(e);
        } finally {
            startUpLatch.countDown();
        }
    }

    @Override
    public void onError(Throwable throwable) {
        if (throwable instanceof HttpStatusException) {
            HttpStatus status = ((HttpStatusException) throwable).getHttpStatus();
            if (status.is5xxServerError()) {
                scheduleFetchInMillis(30 * 1000);
                return;
            }
            throw new IllegalStateException("HTTP status: " + status, throwable);
        }
        throw new IllegalStateException("Unknown error when fetching token", throwable);
    }

    @Override
    public void onComplete() {
    }

    @Override
    public String get() {
        if (atomicToken.get() == null) waitForFirstToken(); //Needed for initial startup
        if (atomicToken.get() == null) throw new IllegalStateException("Initial token could not be retrieved.");
        if (atomicToken.get().getExpiry() == null || atomicToken.get().getExpiry().isBefore(clock.instant()))
            throw new IllegalStateException("Token is expired (or expiration is missing).");
        return atomicToken.get().getToken();
    }

    private void waitForFirstToken() {
        try {
            startUpLatch.await();
        } catch (InterruptedException e) {
            throw new IllegalStateException("Could not get initial token");
        }
    }

}
