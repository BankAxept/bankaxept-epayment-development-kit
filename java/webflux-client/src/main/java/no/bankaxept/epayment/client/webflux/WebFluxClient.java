package no.bankaxept.epayment.client.webflux;

import static java.util.Objects.requireNonNullElse;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Flow.Publisher;
import java.util.function.Function;
import no.bankaxept.epayment.client.base.http.HttpClient;
import no.bankaxept.epayment.client.base.http.HttpResponse;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.publisher.Mono;

public class WebFluxClient implements HttpClient {

  private final WebClient webClient;
  private final Function<Mono<HttpResponse>, Mono<HttpResponse>> transformer;

  public WebFluxClient(String baseUrl) {
    this(baseUrl, Function.identity());
  }

  public WebFluxClient(String baseUrl, Function<Mono<HttpResponse>, Mono<HttpResponse>> transformer) {
    webClient = Optional.ofNullable(WebFluxClient.class.getPackage().getImplementationVersion())
        .map(implementationVersion -> WebClient.builder()
            .defaultHeader("User-Agent", "EppDevKit/" + implementationVersion))
        .orElseGet(WebClient::builder).baseUrl(baseUrl).build();
    this.transformer = requireNonNullElse(transformer, Function.identity());
  }

  @Override
  public Publisher<HttpResponse> get(String uri, Map<String, List<String>> headers) {
    return sendRequest(HttpMethod.GET, uri, headers);
  }

  @Override
  public Publisher<HttpResponse> post(String uri, Publisher<String> bodyPublisher, Map<String, List<String>> headers) {
    return sendRequest(HttpMethod.POST, uri, headers, bodyPublisher);
  }

  @Override
  public Publisher<HttpResponse> delete(String uri, Map<String, List<String>> headers) {
    return sendRequest(HttpMethod.DELETE, uri, headers);
  }

  @Override
  public Publisher<HttpResponse> put(String uri, Publisher<String> bodyPublisher, Map<String, List<String>> headers) {
    return sendRequest(HttpMethod.PUT, uri, headers, bodyPublisher);
  }

  private Publisher<HttpResponse> sendRequest(
      HttpMethod method,
      String uri,
      Map<String, List<String>> headers
  ) {
    return webClient
        .method(method)
        .uri(uri)
        .headers(httpHeaders -> httpHeaders.putAll(headers))
        .retrieve()
        .toEntity(String.class)
        .map(entity -> new HttpResponse(entity.getStatusCode().value(), requireNonNullElse(entity.getBody(), "")))
        .as(transformer)
        .onErrorResume(WebClientResponseException.class, e -> Mono.just(new HttpResponse(
            e.getStatusCode().value(),
            e.getResponseBodyAsString()
        )))
        .as(JdkFlowAdapter::publisherToFlowPublisher);
  }

  private Publisher<HttpResponse> sendRequest(
      HttpMethod method,
      String uri,
      Map<String, List<String>> headers,
      Publisher<String> bodyPublisher
  ) {
    return webClient
        .method(method)
        .uri(uri)
        .headers(httpHeaders -> httpHeaders.putAll(headers))
        .body(BodyInserters.fromProducer(JdkFlowAdapter.flowPublisherToFlux(bodyPublisher), String.class))
        .retrieve()
        .toEntity(String.class)
        .map(entity -> new HttpResponse(entity.getStatusCode().value(), requireNonNullElse(entity.getBody(), "")))
        .as(transformer)
        .onErrorResume(WebClientResponseException.class, e -> Mono.just(new HttpResponse(
            e.getStatusCode().value(),
            e.getResponseBodyAsString()
        )))
        .as(JdkFlowAdapter::publisherToFlowPublisher);
  }

}
