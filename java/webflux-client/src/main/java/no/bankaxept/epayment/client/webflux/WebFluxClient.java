package no.bankaxept.epayment.client.webflux;

import java.util.List;
import java.util.Map;
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
    webClient = WebClient.create(baseUrl.endsWith("/") ? baseUrl : baseUrl + "/");
  }

  public WebFluxClient() {
    webClient = WebClient.create();
  }

  @Override
  public Publisher<HttpResponse> post(String uri, Publisher<String> bodyPublisher, Map<String, List<String>> headers) {
    return sendRequest(uri, bodyPublisher, headers, HttpMethod.POST);
  }

  @Override
  public Publisher<HttpResponse> delete(String uri, Map<String, List<String>> headers) {
    return sendRequest(uri, headers, HttpMethod.DELETE);
  }

  @Override
  public Publisher<HttpResponse> put(String uri, Publisher<String> bodyPublisher, Map<String, List<String>> headers) {
    return sendRequest(uri, bodyPublisher, headers, HttpMethod.PUT);
  }

  private Publisher<HttpResponse> sendRequest(
      String uri,
      Publisher<String> bodyPublisher,
      Map<String, List<String>> headers,
      HttpMethod method
  ) {
    return JdkFlowAdapter.publisherToFlowPublisher(
        setupRequest(uri, headers, method)
            .body(BodyInserters.fromProducer(JdkFlowAdapter.flowPublisherToFlux(bodyPublisher), String.class))
            .exchangeToMono(mapResponse()));
  }

  private Publisher<HttpResponse> sendRequest(String uri, Map<String, List<String>> headers, HttpMethod method) {
    return JdkFlowAdapter.publisherToFlowPublisher(
        setupRequest(uri, headers, method)
            .exchangeToMono(mapResponse()));

  }

  private WebClient.RequestBodySpec setupRequest(String uri, Map<String, List<String>> headers, HttpMethod method) {
    return webClient
        .method(method)
        .uri(uri)
        .headers(httpHeaders -> httpHeaders.putAll(headers));
  }

  private Function<ClientResponse, Mono<HttpResponse>> mapResponse() {
    return clientResponse -> {
      if (clientResponse.statusCode().is2xxSuccessful())
        return clientResponse.bodyToMono(String.class)
            .defaultIfEmpty("")
            .map(v -> new HttpResponse(clientResponse.statusCode().value(), v));
      return Mono.just(new HttpResponse(clientResponse.statusCode().value(), null));
    };
  }
}
