package no.bankaxept.epayment.client.base;

import java.net.URL;
import java.time.Clock;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.Flow;
import java.util.function.Supplier;
import no.bankaxept.epayment.client.base.accesstoken.AccessTokenPublisher;
import no.bankaxept.epayment.client.base.accesstoken.AccessTokenSubscriber;
import no.bankaxept.epayment.client.base.accesstoken.EmptyAccessTokenPublisher;
import no.bankaxept.epayment.client.base.accesstoken.ScheduledAccessTokenPublisher;
import no.bankaxept.epayment.client.base.accesstoken.StaticAccessTokenPublisher;
import no.bankaxept.epayment.client.base.accesstoken.SuppliedAccessTokenPublisher;
import no.bankaxept.epayment.client.base.http.HttpClient;
import no.bankaxept.epayment.client.base.http.HttpResponse;
import no.bankaxept.epayment.client.base.spi.HttpClientProvider;
import reactor.core.publisher.Mono;

public class BaseClient {

  private final static Duration tokenTimeout = Duration.ofSeconds(10);

  private final HttpClient httpClient;
  private final AccessTokenPublisher tokenPublisher;

  private BaseClient(HttpClient httpClient, AccessTokenPublisher tokenPublisher) {
    this.httpClient = httpClient;
    this.tokenPublisher = tokenPublisher;
  }

  private Map<String, List<String>> filterHeaders(
      Map<String, List<String>> headers,
      String correlationId,
      boolean hasBody
  ) {
    var filteredHeaders = new LinkedHashMap<>(headers);
    if (correlationId != null) {
      filteredHeaders.put("X-Correlation-Id", List.of(correlationId));
    }
    if (!(tokenPublisher instanceof EmptyAccessTokenPublisher))
      filteredHeaders.put(
          "Authorization",
          List.of("Bearer " + new AccessTokenSubscriber(tokenPublisher).get(tokenTimeout))
      );
    if (hasBody && !headers.containsKey("Content-Type"))
      filteredHeaders.put("Content-Type", List.of("application/json"));
    return filteredHeaders;
  }

  public Mono<HttpResponse> get(
      String uri,
      Map<String, List<String>> headers
  ) {
    return httpClient.get(uri, filterHeaders(headers, null, false));
  }

  public Mono<HttpResponse> post(
      String uri,
      Flow.Publisher<String> body,
      String correlationId
  ) {
    return post(uri, body, correlationId, Map.of());
  }

  public Mono<HttpResponse> post(
      String uri,
      Flow.Publisher<String> body,
      String correlationId,
      Map<String, List<String>> headers
  ) {
    return httpClient.post(uri, body, filterHeaders(headers, correlationId, true));
  }

  public Mono<HttpResponse> delete(
      String uri,
      String correlationId
  ) {
    return httpClient.delete(uri, filterHeaders(Map.of(), correlationId, false));
  }

  public Mono<HttpResponse> put(
      String uri,
      Flow.Publisher<String> body,
      String correlationId
  ) {
    return httpClient.put(uri, body, filterHeaders(Map.of(), correlationId, true));
  }

  public void shutDown() {
    tokenPublisher.shutDown();
  }

  public static class Builder {

    private final HttpClient httpClient;
    private AccessTokenPublisher tokenPublisher;

    public Builder(URL resourceServerUrl) {
      this.httpClient = ServiceLoader.load(HttpClientProvider.class)
          .findFirst()
          .map(httpClientProvider -> httpClientProvider.create(resourceServerUrl.toString()))
          .orElseThrow();
    }

    public Builder withStaticToken(String token) {
      this.tokenPublisher = new StaticAccessTokenPublisher(token);
      return this;
    }

    public Builder withSuppliedToken(Supplier<String> tokenSupplier) {
      this.tokenPublisher = new SuppliedAccessTokenPublisher(tokenSupplier);
      return this;
    }

    public Builder withScheduledToken(URL authorizationServerUrl, String id, String secret) {
      this.tokenPublisher = new ScheduledAccessTokenPublisher.Builder()
          .httpClient(httpClient)
          .url(authorizationServerUrl)
          .clientCredentials(id, secret)
          .build();
      return this;
    }

    public Builder withScheduledToken(URL authorizationServerUrl, String id, String secret, Clock clock) {
      this.tokenPublisher = new ScheduledAccessTokenPublisher.Builder()
          .httpClient(httpClient)
          .url(authorizationServerUrl)
          .clientCredentials(id, secret)
          .clock(clock)
          .build();
      return this;
    }

    public BaseClient build() {
      return new BaseClient(httpClient, tokenPublisher);
    }
  }


}
