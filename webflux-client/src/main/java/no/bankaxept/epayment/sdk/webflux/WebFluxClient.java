package no.bankaxept.epayment.sdk.webflux;

import no.bankaxept.epayment.sdk.baseclient.HttpClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.adapter.JdkFlowAdapter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow.Publisher;

public class WebFluxClient implements HttpClient {

    private WebClient webClient;

    public WebFluxClient(String baseUrl) {
        webClient = WebClient.create(baseUrl);
    }

    @Override
    public Publisher<String> post(String uri, Publisher<String> bodyPublisher, Map<String, List<String>> headers) {
        return JdkFlowAdapter.publisherToFlowPublisher(
                webClient
                        .post()
                        .uri(uri)
                        .body(bodyPublisher == null ? BodyInserters.empty() : BodyInserters.fromProducer(JdkFlowAdapter.flowPublisherToFlux(bodyPublisher), String.class))
                        .headers(httpHeaders -> httpHeaders.putAll(headers))
                        .exchangeToMono(clientResponse -> clientResponse.bodyToMono(String.class)));
    }

}
