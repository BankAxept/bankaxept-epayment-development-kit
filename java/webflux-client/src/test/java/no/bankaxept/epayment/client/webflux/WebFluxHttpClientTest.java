package no.bankaxept.epayment.client.webflux;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.matching.ContainsPattern;
import no.bankaxept.epayment.client.base.http.HttpClient;
import no.bankaxept.epayment.client.base.http.HttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;


@WireMockTest(httpPort = 8443)
public class WebFluxHttpClientTest {

    private final HttpClient client = new WebFluxClient("http://localhost:8443");

    @AfterEach
    public void cleanUp() {
        assertThat(findUnmatchedRequests()).isEmpty();
    }

    @Test
    public void simple_request_no_body_no_headers(){
        stubFor(post("/test").willReturn(ok()));
        Flux<HttpResponse> fluxPublisher = JdkFlowAdapter.flowPublisherToFlux(client.post("/test", JdkFlowAdapter.publisherToFlowPublisher(Mono.just("")), Map.of()));
        StepVerifier.create(fluxPublisher)
                .verifyComplete(); //Nothing is emitted if response is empty
    }

    @Test
    public void simple_request_empty_body_no_headers(){
        stubFor(post("/test").willReturn(ok().withBody("response")));
        var publisher = client.post("/test", JdkFlowAdapter.publisherToFlowPublisher(Mono.empty()), Map.of());
        StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(publisher))
                .expectNext(new HttpResponse(200, "response"))
                .verifyComplete();
    }

    @Test
    public void simple_request_with_body_and_header(){
        stubFor(post("/test")
                .withHeader("test-header", new ContainsPattern("test-value"))
                .willReturn(ok().withBody("response-body")));
        var resultPublisher = client.post("/test", JdkFlowAdapter.publisherToFlowPublisher(Mono.just("request-body")), Map.of("test-header", List.of("test-value")));
        StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(resultPublisher))
                .expectNext(new HttpResponse(200, "response-body"))
                .verifyComplete();
    }

}