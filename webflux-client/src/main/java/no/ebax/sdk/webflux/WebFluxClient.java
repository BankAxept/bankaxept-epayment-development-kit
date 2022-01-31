package no.ebax.sdk.webflux;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.ebax.sdk.baseclient.ApiClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.adapter.JdkFlowAdapter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow.Publisher;

public class WebFluxClient implements ApiClient {

    private WebClient webClient;
    private ObjectMapper objectMapper = new ObjectMapper();

    public WebFluxClient(String baseUrl) {
        webClient = WebClient.create(baseUrl);
    }

    @Override
    public <T> Publisher<T> post(String uri, Publisher<String> bodyPublisher, Map<String, List<String>> headers, Class<T> tClass) {
        return JdkFlowAdapter.publisherToFlowPublisher(
                webClient
                        .post()
                        .uri(uri)
                        .body(bodyPublisher == null ? BodyInserters.empty() : BodyInserters.fromProducer(JdkFlowAdapter.flowPublisherToFlux(bodyPublisher), String.class))
                        .headers(httpHeaders -> httpHeaders.putAll(headers))
                        .exchangeToMono(clientResponse -> clientResponse.bodyToMono(String.class).mapNotNull(body -> {
                            try {
                                if(tClass.equals(String.class)) return (T) body; //TODO
                                return objectMapper.readValue(body, tClass);
                            } catch (JsonProcessingException e) {
                                throw new IllegalStateException(e);
                            }
                        })));
    }

}
