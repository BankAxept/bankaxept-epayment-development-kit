package no.bankaxept.epayment.sdk.webflux;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.matching.ContainsPattern;
import no.bankaxept.epayment.sdk.baseclient.http.HttpResponse;
import no.bankaxept.epayment.sdk.baseclient.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static org.assertj.core.api.Assertions.assertThat;


@WireMockTest(httpPort = 8443)
public class WebFluxClientTest {

    private HttpClient client = new WebFluxClient("http://localhost:8443");

    @AfterEach
    public void cleanUp() {
        assertThat(findUnmatchedRequests()).isEmpty();
    }

    @Test
    public void simple_request_no_body_no_headers(){
        stubFor(post("/test").willReturn(ok()));
        Flux<HttpResponse> fluxPublisher = JdkFlowAdapter.flowPublisherToFlux(client.post("/test", null, Collections.emptyMap()));
        //No mono will be emitted if the response body is empty
        StepVerifier.create(fluxPublisher)
                .verifyComplete();
    }

    @Test
    public void simple_request_empty_body_no_headers(){
        stubFor(post("/test").willReturn(ok().withBody("response")));
        //No mono will be emitted if the response body is empty
        var publisher = client.post("/test", JdkFlowAdapter.publisherToFlowPublisher(Mono.empty()), Collections.emptyMap());
        StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(publisher))
                .expectNext(new HttpResponse(200, "response"))
                .verifyComplete();
    }

    @Test
    public void simple_request_with_body_and_header(){
        stubFor(post("/test")
                .withHeader("test-header", new ContainsPattern("test-value"))
                .willReturn(ok().withBody("response-body")));
        var resultPublisher = client.post("/test", JdkFlowAdapter.publisherToFlowPublisher(Mono.just("request-body")), Collections.singletonMap("test-header", Collections.singletonList("test-value")));
        StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(resultPublisher))
                .expectNext(new HttpResponse(200, "response-body"))
                .verifyComplete();
    }

}