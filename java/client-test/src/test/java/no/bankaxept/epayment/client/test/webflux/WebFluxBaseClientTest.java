package no.bankaxept.epayment.client.test.webflux;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.forbidden;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.removeStub;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import no.bankaxept.epayment.client.base.AccessFailed;
import no.bankaxept.epayment.client.base.http.HttpResponse;
import no.bankaxept.epayment.client.base.http.HttpStatus;
import no.bankaxept.epayment.client.base.http.HttpStatusException;
import no.bankaxept.epayment.client.test.AbstractBaseClientWireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.adapter.JdkFlowAdapter;
import reactor.test.StepVerifier;
import java.net.MalformedURLException;

public class WebFluxBaseClientTest extends AbstractBaseClientWireMockTest {

  @BeforeEach
  public void setup(WireMockRuntimeInfo wmRuntimeInfo) throws MalformedURLException {
    super.setup(wmRuntimeInfo);
    stubFor(testEndpointMapping());
  }

  @Nested
  @DisplayName("Scheduled token tests")
  public class WebfluxAccessTokenErrorTests {

    @BeforeEach
    public void setup(WireMockRuntimeInfo wmRuntimeInfo) throws MalformedURLException {
      WireMock.stubFor(tokenEndpointMapping(validTokenResponse("scheduled-token")));
      baseClient = createScheduledBaseClient(wmRuntimeInfo.getHttpPort());
      stubFor(testEndpointMapping("scheduled-token"));
    }

    @Test
    public void should_add_all_relevant_headers_with_scheduled_token() {
      StepVerifier.create(baseClient.post("/test", "", "1"))
          .expectNext(new HttpResponse(200, ""))
          .verifyComplete();
    }

    @Test
    public void should_fail_if_client_error_when_fetching_token(WireMockRuntimeInfo wmRuntimeInfo) throws MalformedURLException {
      stubFor(tokenEndpointMapping(forbidden()));
      baseClient = createScheduledBaseClient(wmRuntimeInfo.getHttpPort());
      StepVerifier.create(baseClient.post("/test", "", "1"))
          .expectErrorSatisfies(throwable -> {
            assert throwable instanceof HttpStatusException;
            assert ((HttpStatusException)throwable).getHttpStatus().equals(new HttpStatus(403));
          })
          .verify();
    }

    @Test
    public void should_fail_if_connection_reset_when_fetching_token(WireMockRuntimeInfo wmRuntimeInfo) throws MalformedURLException {
      stubFor(tokenEndpointMapping(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));
      baseClient = createScheduledBaseClient(wmRuntimeInfo.getHttpPort());
      StepVerifier.create(baseClient.post("/test", "", "1"))
          .expectErrorSatisfies(throwable -> {
            assert throwable instanceof WebClientRequestException;
          })
          .verify();
    }

    @Test
    public void new_token_is_fetched_after_error(WireMockRuntimeInfo wmRuntimeInfo) throws MalformedURLException {
      stubFor(tokenEndpointMapping(serverError()));
      baseClient = createScheduledBaseClient(wmRuntimeInfo.getHttpPort());
      StepVerifier.create(baseClient.post("/test", "", "1"))
          .expectErrorSatisfies(throwable -> {
            assert throwable instanceof HttpStatusException;
            assert ((HttpStatusException)throwable).getHttpStatus().equals(new HttpStatus(500));
          })
          .verify();
      removeStub(tokenEndpointMapping(serverError()));
      stubFor(tokenEndpointMapping(validTokenResponse()));
      StepVerifier.create(baseClient.post("/test", "", "1"))
          .expectNext(new HttpResponse(200, ""))
          .verifyComplete();        //Added delay for consistency
    }
  }

  private MappingBuilder testEndpointMapping() {
    return testEndpointMapping("a-token");
  }


  private MappingBuilder testEndpointMapping(String token) {
    return post("/test")
        .withHeader("Authorization", new EqualToPattern("Bearer " + token))
        .withHeader("X-Correlation-Id", new EqualToPattern("1"))
        .willReturn(ok());
  }

}
