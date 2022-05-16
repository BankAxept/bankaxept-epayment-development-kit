package no.bankaxept.epayment.test.client.webflux;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import no.bankaxept.epayment.client.base.BaseClient;
import no.bankaxept.epayment.test.client.AbstractBaseClientWireMockTest;
import no.bankaxept.epayment.client.base.AccessFailed;
import no.bankaxept.epayment.client.base.http.HttpResponse;
import no.bankaxept.epayment.client.base.http.HttpStatus;
import no.bankaxept.epayment.client.base.http.HttpStatusException;
import org.junit.jupiter.api.*;
import reactor.adapter.JdkFlowAdapter;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class WebFluxBaseClientTest extends AbstractBaseClientWireMockTest {

    @BeforeEach
    public void setup(WireMockRuntimeInfo wmRuntimeInfo) {
        super.setup(wmRuntimeInfo);
        stubFor(testEndpointMapping());
    }

    @Test
    public void should_be_possible_to_override_headers(WireMockRuntimeInfo wmRuntimeInfo) {
        baseClient = BaseClient.withStaticToken("http://localhost:" + wmRuntimeInfo.getHttpPort(), "static-token");
        stubFor(post("/test")
                .withHeader("Authorization", new EqualToPattern("Bearer a-token"))
                .withHeader("X-Correlation-Id", new EqualToPattern("1"))
                .willReturn(ok()));
        StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(baseClient.post("/test", emptyPublisher(), "2",
                Map.of("Authorization", List.of("Bearer a-token"), "X-Correlation-Id", List.of("1")))))
                .expectNext(new HttpResponse(200, ""))
                .verifyComplete();
    }

    @Nested
    @DisplayName("Scheduled token tests")
    public class WebfluxAccessTokenErrorTests {

        @BeforeEach
        public void setup(WireMockRuntimeInfo wmRuntimeInfo) {
            WireMock.stubFor(tokenEndpointMapping(validTokenResponse("scheduled-token")));
            baseClient = createScheduledBaseClient(wmRuntimeInfo.getHttpPort());
            stubFor(testEndpointMapping("scheduled-token"));
        }

        @Test
        public void should_add_all_relevant_headers_with_scheduled_token() {
            StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(baseClient.post("/test", emptyPublisher(), "1")))
                    .expectNext(new HttpResponse(200, ""))
                    .verifyComplete();
        }

        @Test
        public void should_fail_if_client_error_when_fetching_token(WireMockRuntimeInfo wmRuntimeInfo) {
            stubFor(tokenEndpointMapping(forbidden()));
            baseClient = createScheduledBaseClient(wmRuntimeInfo.getHttpPort());
            assertThatThrownBy(() -> baseClient.post("/test", emptyPublisher(), "1")).isInstanceOf(AccessFailed.class)
                    .getCause().isInstanceOf(HttpStatusException.class)
                    .hasFieldOrPropertyWithValue("HttpStatus", new HttpStatus(403));
        }

        @Test
        public void should_fail_if_connection_reset_when_fetching_token(WireMockRuntimeInfo wmRuntimeInfo) {
            stubFor(tokenEndpointMapping(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));
            baseClient = createScheduledBaseClient(wmRuntimeInfo.getHttpPort());
            assertThatThrownBy(() -> baseClient.post("/test", emptyPublisher(), "1")).isInstanceOf(AccessFailed.class);
        }

        @Test
        public void new_token_is_fetched_after_error(WireMockRuntimeInfo wmRuntimeInfo) {
            stubFor(tokenEndpointMapping(serverError()));
            baseClient = createScheduledBaseClient(wmRuntimeInfo.getHttpPort());
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
        return testEndpointMapping("a-token");
    }


    private MappingBuilder testEndpointMapping(String token) {
        return post("/test")
                .withHeader("Authorization", new EqualToPattern("Bearer " + token))
                .withHeader("X-Correlation-Id", new EqualToPattern("1"))
                .willReturn(ok());
    }

}