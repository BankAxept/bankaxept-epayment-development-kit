package no.bankaxept.epayment.client.base;

import no.bankaxept.epayment.client.base.accesstoken.AccessTokenSupplier;
import no.bankaxept.epayment.client.base.http.HttpClient;
import no.bankaxept.epayment.client.base.http.HttpResponse;
import no.bankaxept.epayment.client.base.spi.HttpClientProvider;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.ScheduledExecutorService;

public class BaseClient {

    private AccessTokenSupplier tokenSupplier;
    private HttpClient httpClient;

    public BaseClient(String baseurl, String apimKey, String username, String password) {
        this(baseurl, apimKey, username, password, Clock.systemDefaultZone(), Executors.newScheduledThreadPool(1));
    }

    public BaseClient(String baseurl, String apimKey, String username, String password, Clock clock, ScheduledExecutorService scheduler) {
        httpClient = ServiceLoader.load(HttpClientProvider.class)
                .findFirst()
                .map(httpClientProvider -> httpClientProvider.create(baseurl))
                .orElseThrow();
        this.tokenSupplier = new AccessTokenSupplier("/token", apimKey, username, password, clock, scheduler, httpClient);
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
        allHeaders.put("Authorization", List.of("Bearer " + tokenSupplier.get()));
        return httpClient.post(uri, body, allHeaders);
    }

}
