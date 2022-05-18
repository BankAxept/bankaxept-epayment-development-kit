package no.bankaxept.epayment.client.base;

import no.bankaxept.epayment.client.base.accesstoken.*;
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
import java.util.function.Supplier;

public class BaseClient {
    private final AccessTokenPublisher tokenPublisher;
    private final HttpClient httpClient;
    private final Duration tokenTimeout = Duration.ofSeconds(10);

    public BaseClient(String baseurl, String apimKey, String username, String password) {
        this(baseurl, apimKey, username, password, Clock.systemDefaultZone());
    }

    public BaseClient(String baseurl, String apimKey, String username, String password, Clock clock) {
        httpClient = ServiceLoader.load(HttpClientProvider.class)
                .findFirst()
                .map(httpClientProvider -> httpClientProvider.create(baseurl))
                .orElseThrow();
        this.tokenPublisher = new ScheduledAccessTokenPublisher("/bankaxept-epayment/access-token-api/v1/accesstoken", apimKey, username, password, clock, Executors.newScheduledThreadPool(1), httpClient);
    }

    private BaseClient(String baseurl, String token) {
        httpClient = ServiceLoader.load(HttpClientProvider.class)
                .findFirst()
                .map(httpClientProvider -> httpClientProvider.create(baseurl))
                .orElseThrow();
        this.tokenPublisher = new StaticAccessTokenPublisher(token);
    }

    private BaseClient(String baseurl) {
        httpClient = ServiceLoader.load(HttpClientProvider.class)
                .findFirst()
                .map(httpClientProvider -> httpClientProvider.create(baseurl))
                .orElseThrow();
        this.tokenPublisher = new EmptyAccessTokenPublisher();
    }

    private BaseClient(String baseurl, Supplier<String> tokenSupplier) {
        httpClient = ServiceLoader.load(HttpClientProvider.class)
                .findFirst()
                .map(httpClientProvider -> httpClientProvider.create(baseurl))
                .orElseThrow();
        this.tokenPublisher = new SuppliedAccessTokenPublisher(tokenSupplier);
    }

    public static BaseClient withStaticToken(String baseurl, String token) {
        return new BaseClient(baseurl, token);
    }

    public static BaseClient withoutToken(String baseurl) {
        return new BaseClient(baseurl);
    }

    public static BaseClient withSuppliedToken(String baseurl, Supplier<String> tokenSupplier) {
        return new BaseClient(baseurl, tokenSupplier);
    }

    public Flow.Publisher<HttpResponse> post(
            String uri,
            Flow.Publisher<String> body,
            String correlationId
    ) {
        return post(uri, body, correlationId, Map.of());
    }

    public Flow.Publisher<HttpResponse> post(
            String uri,
            Flow.Publisher<String> body,
            String correlationId,
            Map<String, List<String>> headers
    ) {
        var allHeaders = new LinkedHashMap<>(headers);
        if (!headers.containsKey("X-Correlation-Id"))
            allHeaders.put("X-Correlation-Id", List.of(correlationId));
        if (!headers.containsKey("Authorization") && !(tokenPublisher instanceof EmptyAccessTokenPublisher))
            allHeaders.put("Authorization", List.of("Bearer " + new AccessTokenSubscriber(tokenPublisher).get(tokenTimeout)));
        if (!headers.containsKey("Content-Type"))
            allHeaders.put("Content-Type", List.of("application/json"));
        return httpClient.post(uri, body, allHeaders);
    }

    public void shutDown() {
        tokenPublisher.shutDown();
    }
}
