package no.bankaxept.epayment.client.base;

import no.bankaxept.epayment.client.base.accesstoken.AccessTokenPublisher;
import no.bankaxept.epayment.client.base.accesstoken.AccessTokenSubscriber;
import no.bankaxept.epayment.client.base.http.HttpClient;
import no.bankaxept.epayment.client.base.http.HttpResponse;
import no.bankaxept.epayment.client.base.spi.HttpClientProvider;

import java.time.Clock;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.*;

public class BaseClient {
    private final AccessTokenPublisher tokenPublisher;
    private final HttpClient httpClient;

    public BaseClient(String baseurl, String apimKey, String username, String password) {
        this(baseurl, apimKey, username, password, Clock.systemDefaultZone(), Executors.newScheduledThreadPool(1));
    }

    public BaseClient(String baseurl, String apimKey, String username, String password, Clock clock, ScheduledExecutorService scheduler) {
        httpClient = ServiceLoader.load(HttpClientProvider.class)
                .findFirst()
                .map(httpClientProvider -> httpClientProvider.create(baseurl))
                .orElseThrow();
        this.tokenPublisher = new AccessTokenPublisher("/token", apimKey, username, password, clock, scheduler, httpClient);
    }

    public Flow.Publisher<HttpResponse> post(
            String uri,
            Flow.Publisher<String> body,
            String correlationId
    ) throws ExecutionException, InterruptedException, TimeoutException {
        return post(uri, body, correlationId, Map.of());
    }

    public Flow.Publisher<HttpResponse> post(
            String uri,
            Flow.Publisher<String> body,
            String correlationId,
            Map<String, List<String>> headers
    ) throws ExecutionException, InterruptedException, TimeoutException {
        var allHeaders = new LinkedHashMap<>(headers);
        allHeaders.put("X-Correlation-Id", List.of(correlationId));
        allHeaders.put("Authorization", List.of("Bearer " + new AccessTokenSubscriber(tokenPublisher).get(Duration.ofSeconds(2))));
        return httpClient.post(uri, body, allHeaders);
    }
}
