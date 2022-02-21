package no.bankaxept.epayment.client.webflux;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import no.bankaxept.epayment.client.base.BaseClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest(httpPort = 8443)
public class WebFluxBaseClientTest {

    private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
    private BaseClient baseClient; //Because it fetches token on start, it needs to be started after setting up wiremock
    private String tokenResponseFromFile = readJsonFromFile("token-response.json").replace("123", Long.toString(clock.instant().plus(20, ChronoUnit.MINUTES).toEpochMilli()));

    private Flow.Publisher<String> emptyPublisher() {
        return JdkFlowAdapter.publisherToFlowPublisher(Mono.just(""));
    }

    private String readJsonFromFile(String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(filename).getPath())));
    }

    public WebFluxBaseClientTest() throws IOException {
    }

    @BeforeEach
    public void setup() {
        stubFor(testEndpointMapping());
    }

    @Test
    public void should_add_all_relevant_headers() throws ExecutionException, InterruptedException, TimeoutException {
        stubFor(tokenEndpointMapping(validTokenResponse()));
        baseClient = createBaseClient();
        StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(baseClient.post("/test", emptyPublisher(), "1")))
                .verifyComplete();
    }

    @Test
    public void should_fail_if_client_error() {
        stubFor(tokenEndpointMapping(forbidden()));
        baseClient = createBaseClient();
        assertThatThrownBy(() -> baseClient.post("/test", emptyPublisher(), "1")).isInstanceOf(ExecutionException.class);
    }

    @Test
    public void should_fail_if() {
        stubFor(tokenEndpointMapping(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));
        baseClient = createBaseClient();
        assertThatThrownBy(() -> baseClient.post("/test", emptyPublisher(), "1")).isInstanceOf(ExecutionException.class);
    }

    @Test
    public void should_retry_if_server_error_and_work() throws ExecutionException, InterruptedException, TimeoutException {
        stubFor(tokenEndpointMapping(serverError())
                .inScenario("Token")
                .whenScenarioStateIs(Scenario.STARTED)
                .willSetStateTo("Second token"));
        stubFor(tokenEndpointMapping(validTokenResponse())
                .inScenario("Token")
                .whenScenarioStateIs("Second token"));
        baseClient = createBaseClientWithTimeout(Duration.ofSeconds(10));
        StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(baseClient.post("/test", emptyPublisher(), "1")))
                .verifyComplete();        //Added delay for consistency
    }

    private BaseClient createBaseClient() {
        return new BaseClient("http://localhost:8443", "key", "username", "password", clock, Duration.ofSeconds(1));
    }

    private BaseClient createBaseClientWithTimeout(Duration timeout) {
        return new BaseClient("http://localhost:8443", "key", "username", "password", clock, timeout);
    }

    private MappingBuilder testEndpointMapping() {
        return post("/test")
                .withHeader("Authorization", new EqualToPattern("Bearer a-token"))
                .withHeader("X-Correlation-Id", new EqualToPattern("1"))
                .willReturn(ok());
    }

    private MappingBuilder tokenEndpointMapping(ResponseDefinitionBuilder responseBuilder) {
        return post("/token")
                .withHeader("Ocp-Apim-Subscription-Key", new EqualToPattern("key"))
                .withBasicAuth("username", "password")
                .willReturn(responseBuilder);
    }

    private ResponseDefinitionBuilder validTokenResponse() {
        return ok().withBody(tokenResponseFromFile);
    }

}