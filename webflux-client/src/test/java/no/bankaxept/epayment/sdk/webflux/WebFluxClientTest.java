package no.bankaxept.epayment.sdk.webflux;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.matching.ContainsPattern;
import no.bankaxept.epayment.sdk.baseclient.http.HttpResponse;
import no.bankaxept.epayment.sdk.baseclient.HttpClient;
import org.junit.jupiter.api.Test;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;


@WireMockTest(httpPort = 8443)
public class WebFluxClientTest {

    private HttpClient client = new WebFluxClient("http://localhost:8443");

    @Test
    public void simple_request_no_body_no_headers(){
        stubFor(post("/test").willReturn(ok()));
        var publisher = client.post("/test", null, Collections.emptyMap());
        StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(publisher))
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