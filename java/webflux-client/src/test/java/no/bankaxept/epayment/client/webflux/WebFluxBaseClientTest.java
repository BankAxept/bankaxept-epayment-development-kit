package no.bankaxept.epayment.client.webflux;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import no.bankaxept.client.test.AbstractBaseClientWireMockTest;
import no.bankaxept.epayment.client.base.AccessFailed;
import no.bankaxept.epayment.client.base.http.HttpStatus;
import no.bankaxept.epayment.client.base.http.HttpStatusException;
import org.junit.jupiter.api.*;
import reactor.adapter.JdkFlowAdapter;
import reactor.test.StepVerifier;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class WebFluxBaseClientTest extends AbstractBaseClientWireMockTest {

    @BeforeEach
    public void setup() {
        super.setup();
        stubFor(testEndpointMapping());
    }

    @Test
    public void should_add_all_relevant_headers() {
        StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(baseClient.post("/test", emptyPublisher(), "1")))
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
        public void should_fail_if_client_error_when_fetching_token() {
            stubFor(tokenEndpointMapping(forbidden()));
            baseClient = createBaseClient();
            assertThatThrownBy(() -> baseClient.post("/test", emptyPublisher(), "1")).isInstanceOf(AccessFailed.class)
                    .getCause().isInstanceOf(HttpStatusException.class)
                    .hasFieldOrPropertyWithValue("HttpStatus", new HttpStatus(403));
        }

        @Test
        public void should_fail_if_connection_reset_when_fetching_token() {
            stubFor(tokenEndpointMapping(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));
            baseClient = createBaseClient();
            assertThatThrownBy(() -> baseClient.post("/test", emptyPublisher(), "1")).isInstanceOf(AccessFailed.class);
        }

        @Test
        public void new_token_is_fetched_after_error() {
            stubFor(tokenEndpointMapping(serverError()));
            baseClient = createBaseClient();
            assertThatThrownBy(() -> baseClient.post("/test", emptyPublisher(), "1")).isInstanceOf(AccessFailed.class)
                    .getCause().isInstanceOf(HttpStatusException.class)
                    .hasFieldOrPropertyWithValue("HttpStatus", new HttpStatus(500));
            removeStub(tokenEndpointMapping(serverError()));
            stubFor(tokenEndpointMapping(validTokenResponse()));
            StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(baseClient.post("/test", emptyPublisher(), "1")))
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