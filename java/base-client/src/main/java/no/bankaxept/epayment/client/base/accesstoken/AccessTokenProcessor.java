package no.bankaxept.epayment.client.base.accesstoken;

import no.bankaxept.epayment.client.base.http.HttpClient;
import no.bankaxept.epayment.client.base.http.HttpResponse;
import no.bankaxept.epayment.client.base.http.HttpStatus;
import no.bankaxept.epayment.client.base.http.HttpStatusException;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class AccessTokenProcessor extends SubmissionPublisher<String> implements Flow.Processor<HttpResponse, String> {
    private final ScheduledExecutorService scheduler;
    private final Clock clock;

    private HttpClient httpClient;
    private final String uri;
    private final LinkedHashMap<String, List<String>> headers;

    private AtomicReference<AccessToken> atomicToken = new AtomicReference<>();
    private AtomicReference<Throwable> atomicFinalThrowable = new AtomicReference<>();

    public AccessTokenProcessor(String uri, String apimKey, String username, String password, Clock clock, ScheduledExecutorService scheduler, HttpClient httpClient) {
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
        SubmissionPublisher<String> emptyPublisher = new SubmissionPublisher<>();
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
                throw new HttpStatusException(item.getStatus(), "Error when fetching token");
            }
            atomicToken.set(AccessToken.parse(item.getBody()));
            submit(atomicToken.get().getToken());
            scheduleFetchInMillis(atomicToken.get().millisUntilTenMinutesBeforeExpiry(clock));
        } catch (Exception e) {
            onError(e);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        if (throwable instanceof HttpStatusException) {
            HttpStatus status = ((HttpStatusException) throwable).getHttpStatus();
            if (status.is5xxServerError()) {
                scheduleFetchInMillis(5 * 1000);
                return;
            }
        }
        atomicFinalThrowable.set(throwable);
        getSubscribers().forEach(subscriber ->
                subscriber.onError(throwable));
    }

    @Override
    public void onComplete() {
    }

    @Override
    public void subscribe(Flow.Subscriber<? super String> subscriber) {
        var token = atomicToken.get();
        var exception = atomicFinalThrowable.get();
        if (token != null && token.getExpiry().isBefore(clock.instant()))
            subscriber.onNext(token.getToken());
        else if (exception != null)
            subscriber.onError(exception);
        else
            super.subscribe(subscriber);
    }
}
