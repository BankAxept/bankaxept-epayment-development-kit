package no.bankaxept.epayment.sdk.webflux;

import no.bankaxept.epayment.sdk.baseclient.http.HttpResponse;
import no.bankaxept.epayment.sdk.baseclient.HttpClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow.Publisher;

public class WebFluxClient implements HttpClient {

    private WebClient webClient;

    public WebFluxClient(String baseUrl) {
        webClient = WebClient.create(baseUrl);
    }

    @Override
    public Publisher<HttpResponse> post(String uri, Publisher<String> bodyPublisher, Map<String, List<String>> headers) {
        return JdkFlowAdapter.publisherToFlowPublisher(
                webClient
                        .post()
                        .uri(uri)
                        .body(bodyPublisher == null ? BodyInserters.empty() : BodyInserters.fromProducer(JdkFlowAdapter.flowPublisherToFlux(bodyPublisher), String.class))
                        .headers(httpHeaders -> httpHeaders.putAll(headers))
                        .exchangeToMono(clientResponse -> {
                            if (clientResponse.statusCode().is2xxSuccessful())
                                return clientResponse.bodyToMono(String.class).map(v -> new HttpResponse(clientResponse.statusCode().value(), v));
                            return Mono.just(new HttpResponse(clientResponse.statusCode().value(), null));
                        }));
    }

}
