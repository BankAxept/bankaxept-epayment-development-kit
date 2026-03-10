package no.bankaxept.epayment.client.webflux;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Flow.Publisher;

import java.util.function.Function;
import no.bankaxept.epayment.client.base.http.HttpClient;
import no.bankaxept.epayment.client.base.http.HttpResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.publisher.Mono;

import static java.util.Objects.requireNonNullElse;

public class WebFluxClient implements HttpClient {

  private final WebClient webClient;
  private final Function<Mono<HttpResponse>, Mono<HttpResponse>> transformer;

  public WebFluxClient(String baseUrl, Function<Mono<HttpResponse>, Mono<HttpResponse>> transformer1) {
    webClient = Optional.ofNullable(WebFluxClient.class.getPackage().getImplementationVersion())
        .map(implementationVersion -> WebClient.builder()
            .defaultHeader("User-Agent", "EppDevKit/" + implementationVersion))
        .orElseGet(WebClient::builder).baseUrl(baseUrl).build();
    this.transformer = transformer1;
  }

  @Override
  public Publisher<HttpResponse> get(String uri, Map<String, List<String>> headers) {
    return sendAndRetrieve(uri, headers, HttpMethod.GET);
  }

  @Override
  public Publisher<HttpResponse> post(String uri, Publisher<String> bodyPublisher, Map<String, List<String>> headers) {
    return sendAndRetrieve(uri, bodyPublisher, headers, HttpMethod.POST);
  }

  @Override
  public Publisher<HttpResponse> delete(String uri, Map<String, List<String>> headers) {
    return sendAndRetrieve(uri, headers, HttpMethod.DELETE);
  }

  @Override
  public Publisher<HttpResponse> put(String uri, Publisher<String> bodyPublisher, Map<String, List<String>> headers) {
    return sendAndRetrieve(uri, bodyPublisher, headers, HttpMethod.PUT);
  }

  private Publisher<HttpResponse> sendAndRetrieve(String uri, Map<String, List<String>> headers, HttpMethod method) {
    return buildRequest(uri, headers, method)
        .retrieve()
        .toEntity(String.class)
        .flatMap(this::toHttpResponse)
        .as(JdkFlowAdapter::publisherToFlowPublisher);
  }

  private Publisher<HttpResponse> sendAndRetrieve(
      String uri,
      Publisher<String> bodyPublisher,
      Map<String, List<String>> headers,
      HttpMethod method
  ) {
    return buildRequest(uri, headers, method)
        .body(BodyInserters.fromProducer(JdkFlowAdapter.flowPublisherToFlux(bodyPublisher), String.class))
        .retrieve()
        .toEntity(String.class)
        .flatMap(this::toHttpResponse)
        .as(JdkFlowAdapter::publisherToFlowPublisher);
  }

  private WebClient.RequestBodySpec buildRequest(String uri, Map<String, List<String>> headers, HttpMethod method) {
    return webClient
        .method(method)
        .uri(uri)
        .headers(httpHeaders -> httpHeaders.putAll(headers));
  }

  private Mono<HttpResponse> toHttpResponse(ResponseEntity<String> entity) {
    return Mono.just(new HttpResponse(entity.getStatusCode().value(), requireNonNullElse(entity.getBody(), "")))
        .as(transformer)
        .onErrorResume(err -> {
          if (err instanceof WebClientResponseException responseException) {
            return Mono.just(new HttpResponse(
                responseException.getStatusCode().value(),
                responseException.getResponseBodyAsString()
            ));
          }
          return Mono.error(err);
        });
  }
}
