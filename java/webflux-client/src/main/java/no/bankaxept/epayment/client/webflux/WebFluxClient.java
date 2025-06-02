package no.bankaxept.epayment.client.webflux;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Flow.Publisher;
import java.util.function.Function;
import no.bankaxept.epayment.client.base.http.HttpClient;
import no.bankaxept.epayment.client.base.http.HttpResponse;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.publisher.Mono;

public class WebFluxClient implements HttpClient {

  private final WebClient webClient;

  public WebFluxClient(String baseUrl) {
    webClient = Optional.ofNullable(WebFluxClient.class.getPackage().getImplementationVersion())
        .map(implementationVersion -> WebClient.builder()
            .defaultHeader("User-Agent", "EppDevKit/" + implementationVersion))
        .orElseGet(WebClient::builder).baseUrl(baseUrl).build();
  }

  public WebFluxClient() {
    webClient = Optional.ofNullable(WebFluxClient.class.getPackage().getImplementationVersion())
        .map(implementationVersion -> WebClient.builder()
            .defaultHeader("User-Agent", "EppDevKit/" + implementationVersion))
        .orElseGet(WebClient::builder).build();
  }

  @Override
  public Mono<HttpResponse> get(String uri, Map<String, List<String>> headers) {
    return sendRequest(uri, headers, HttpMethod.GET);
  }

  @Override
  public Mono<HttpResponse> post(String uri, Publisher<String> bodyPublisher, Map<String, List<String>> headers) {
    return sendRequest(uri, bodyPublisher, headers, HttpMethod.POST);
  }

  @Override
  public Mono<HttpResponse> delete(String uri, Map<String, List<String>> headers) {
    return sendRequest(uri, headers, HttpMethod.DELETE);
  }

  @Override
  public Mono<HttpResponse> put(String uri, Publisher<String> bodyPublisher, Map<String, List<String>> headers) {
    return sendRequest(uri, bodyPublisher, headers, HttpMethod.PUT);
  }

  private Mono<HttpResponse> sendRequest(
      String uri,
      Publisher<String> bodyPublisher,
      Map<String, List<String>> headers,
      HttpMethod method
  ) {
    return setupRequest(uri, headers, method)
            .body(BodyInserters.fromProducer(JdkFlowAdapter.flowPublisherToFlux(bodyPublisher), String.class))
            .exchangeToMono(mapResponse());
  }

  private Mono<HttpResponse> sendRequest(String uri, Map<String, List<String>> headers, HttpMethod method) {
    return setupRequest(uri, headers, method)
            .exchangeToMono(mapResponse());

  }

  private WebClient.RequestBodySpec setupRequest(String uri, Map<String, List<String>> headers, HttpMethod method) {
    return webClient
        .method(method)
        .uri(uri)
        .headers(httpHeaders -> httpHeaders.putAll(headers));
  }

  private Function<ClientResponse, Mono<HttpResponse>> mapResponse() {
    return clientResponse -> clientResponse.bodyToMono(String.class)
        .defaultIfEmpty("")
        .map(v -> new HttpResponse(clientResponse.statusCode().value(), v));
  }
}
