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
import java.util.function.Function;
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

    private BaseClient(String baseurl, AccessTokenPublisher tokenPublisher) {
        httpClient = ServiceLoader.load(HttpClientProvider.class)
                .findFirst()
                .map(httpClientProvider -> httpClientProvider.create(baseurl))
                .orElseThrow();
        this.tokenPublisher = tokenPublisher;
    }

    public static BaseClient withStaticToken(String baseurl, String token) {
        return new BaseClient(baseurl, new StaticAccessTokenPublisher(token));
    }

    public static BaseClient withoutToken(String baseurl) {
        return new BaseClient(baseurl, new EmptyAccessTokenPublisher());
    }

    public static BaseClient withSuppliedToken(String baseurl, Supplier<String> tokenSupplier) {
        return new BaseClient(baseurl, new SuppliedAccessTokenPublisher(tokenSupplier));
    }

    private Map<String, List<String>> filterHeaders(Map<String, List<String>> headers, String correlationId, boolean hasBody) {
        var filteredHeaders = new LinkedHashMap<>(headers);
        filteredHeaders.put("X-Correlation-Id", List.of(correlationId));
        if (!(tokenPublisher instanceof EmptyAccessTokenPublisher))
            filteredHeaders.put("Authorization", List.of("Bearer " + new AccessTokenSubscriber(tokenPublisher).get(tokenTimeout)));
        if (hasBody && !headers.containsKey("Content-Type"))
            filteredHeaders.put("Content-Type", List.of("application/json"));
        return filteredHeaders;
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
        return httpClient.post(uri, body, filterHeaders(headers, correlationId, true));
    }

    public Flow.Publisher<HttpResponse> delete(
            String uri,
            String correlationId,
            Map<String, List<String>> headers
    ) {
        return httpClient.delete(uri, filterHeaders(headers, correlationId, false));
    }


    public Flow.Publisher<HttpResponse> put(
            String uri,
            String correlationId,
            Map<String, List<String>> headers
    ) {
        return httpClient.put(uri, filterHeaders(headers, correlationId, false));
    }

    public void shutDown() {
        tokenPublisher.shutDown();
    }
}
