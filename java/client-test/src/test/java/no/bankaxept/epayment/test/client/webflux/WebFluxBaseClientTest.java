package no.bankaxept.epayment.test.client.webflux;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import no.bankaxept.epayment.test.client.AbstractBaseClientWireMockTest;
import no.bankaxept.epayment.client.base.AccessFailed;
import no.bankaxept.epayment.client.base.http.HttpResponse;
import no.bankaxept.epayment.client.base.http.HttpStatus;
import no.bankaxept.epayment.client.base.http.HttpStatusException;
import org.junit.jupiter.api.*;
import reactor.adapter.JdkFlowAdapter;
import reactor.test.StepVerifier;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class WebFluxBaseClientTest extends AbstractBaseClientWireMockTest {

    @BeforeEach
    public void setup(WireMockRuntimeInfo wmRuntimeInfo) {
        super.setup(wmRuntimeInfo);
        stubFor(testEndpointMapping());
    }

    @Test
    public void should_add_all_relevant_headers() {
        StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(baseClient.post("/test", emptyPublisher(), "1")))
                .expectNext(new HttpResponse(200, ""))
                .verifyComplete();
    }

    @Nested
    @DisplayName("Access token errors")
    public class WebfluxAccessTokenErrorTests {

        @BeforeEach
        public void setup() {
            stubFor(testEndpointMapping());
        }

        @Test
        public void should_fail_if_client_error_when_fetching_token(WireMockRuntimeInfo wmRuntimeInfo) {
            stubFor(tokenEndpointMapping(forbidden()));
            baseClient = createBaseClient(wmRuntimeInfo.getHttpPort());
            assertThatThrownBy(() -> baseClient.post("/test", emptyPublisher(), "1")).isInstanceOf(AccessFailed.class)
                    .getCause().isInstanceOf(HttpStatusException.class)
                    .hasFieldOrPropertyWithValue("HttpStatus", new HttpStatus(403));
        }

        @Test
        public void should_fail_if_connection_reset_when_fetching_token(WireMockRuntimeInfo wmRuntimeInfo) {
            stubFor(tokenEndpointMapping(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));
            baseClient = createBaseClient(wmRuntimeInfo.getHttpPort());
            assertThatThrownBy(() -> baseClient.post("/test", emptyPublisher(), "1")).isInstanceOf(AccessFailed.class);
        }

        @Test
        public void new_token_is_fetched_after_error(WireMockRuntimeInfo wmRuntimeInfo) {
            stubFor(tokenEndpointMapping(serverError()));
            baseClient = createBaseClient(wmRuntimeInfo.getHttpPort());
            assertThatThrownBy(() -> baseClient.post("/test", emptyPublisher(), "1")).isInstanceOf(AccessFailed.class)
                    .getCause().isInstanceOf(HttpStatusException.class)
                    .hasFieldOrPropertyWithValue("HttpStatus", new HttpStatus(500));
            removeStub(tokenEndpointMapping(serverError()));
            stubFor(tokenEndpointMapping(validTokenResponse()));
            StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(baseClient.post("/test", emptyPublisher(), "1")))
                    .expectNext(new HttpResponse(200, ""))
                    .verifyComplete();        //Added delay for consistency
        }
    }

    private MappingBuilder testEndpointMapping() {
        return post("/test")
                .withHeader("Authorization", new EqualToPattern("Bearer a-token"))
                .withHeader("X-Correlation-Id", new EqualToPattern("1"))
                .willReturn(ok());
    }

}