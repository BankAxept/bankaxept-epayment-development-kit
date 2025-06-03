package no.bankaxept.epayment.client.test.webflux;

import static com.github.tomakehurst.wiremock.client.WireMock.findUnmatchedRequests;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.matching.ContainsPattern;
import java.util.List;
import java.util.Map;
import no.bankaxept.epayment.client.base.http.HttpClient;
import no.bankaxept.epayment.client.base.http.HttpResponse;
import no.bankaxept.epayment.client.webflux.WebFluxClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


@WireMockTest
public class WebFluxHttpClientTest {

  private HttpClient client;

  @BeforeEach
  public void setup(WireMockRuntimeInfo wmRuntimeInfo) {
    client = new WebFluxClient("http://localhost:" + wmRuntimeInfo.getHttpPort());
  }

  @AfterEach
  public void cleanUp() {
    assertThat(findUnmatchedRequests()).isEmpty();
  }

  @Test
  public void simple_request_no_body_no_headers() {
    stubFor(post("/test").willReturn(ok()));
    Mono<HttpResponse> monoPublisher = client.post(
        "/test",
        "",
        Map.of()
    );
    StepVerifier.create(monoPublisher)
        .expectNext(new HttpResponse(200, ""))
        .verifyComplete();
  }

  @Test
  public void simple_request_empty_body_no_headers() {
    stubFor(post("/test").willReturn(ok().withBody("response")));
    var publisher = client.post("/test", "", Map.of());
    StepVerifier.create(publisher)
        .expectNext(new HttpResponse(200, "response"))
        .verifyComplete();
  }

  @Test
  public void simple_request_with_body_and_header() {
    stubFor(post("/test")
        .withHeader("test-header", new ContainsPattern("test-value"))
        .willReturn(ok().withBody("response-body")));
    var resultPublisher = client.post(
        "/test",
        "request-body",
        Map.of("test-header", List.of("test-value"))
    );
    StepVerifier.create(resultPublisher)
        .expectNext(new HttpResponse(200, "response-body"))
        .verifyComplete();
  }

}
