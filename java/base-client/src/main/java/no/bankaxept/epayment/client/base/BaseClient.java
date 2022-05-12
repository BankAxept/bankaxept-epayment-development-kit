package no.bankaxept.epayment.client.base;

import no.bankaxept.epayment.client.base.accesstoken.AccessTokenProcessor;
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
    private final AccessTokenProcessor tokenPublisher;
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
        this.tokenPublisher = new AccessTokenProcessor("/bankaxept-epayment/access-token-api/v1/accesstoken", apimKey, username, password, clock, Executors.newScheduledThreadPool(1), httpClient);
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
        allHeaders.put("X-Correlation-Id", List.of(correlationId));
        allHeaders.put("Authorization", List.of("Bearer " + new AccessTokenSubscriber(tokenPublisher).get(tokenTimeout)));
        return httpClient.post(uri, body, allHeaders);
    }

    public void shutDown() {
        tokenPublisher.shutDown();
    }
}
