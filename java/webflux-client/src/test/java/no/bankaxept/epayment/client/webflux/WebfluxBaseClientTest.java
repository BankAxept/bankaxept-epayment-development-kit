package no.bankaxept.epayment.client.webflux;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
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
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest(httpPort = 8443)
public class WebfluxBaseClientTest {

    private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
    private ScheduledExecutorService schedulerSpy;
    private BaseClient baseClient; //Because it fetches token on start, it needs to be started after setting up wiremock
    private String tokenResponseFromFile = readJsonFromFile("token-response.json").replace("123", Long.toString(clock.instant().plus(20, ChronoUnit.MINUTES).toEpochMilli()));

    private Flow.Publisher<String> emptyPublisher() {
        return JdkFlowAdapter.publisherToFlowPublisher(Mono.just(""));
    }

    private String readJsonFromFile(String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(filename).getPath())));
    }

    public WebfluxBaseClientTest() throws IOException {
    }

    @BeforeEach
    public void setup() {
        schedulerSpy = Mockito.spy(new ScheduledThreadPoolExecutor(1));
    }

    @Test
    public void should_add_all_relevant_headers_and_schedule_refresh() throws ExecutionException, InterruptedException, TimeoutException {
        stubTokenEndpoint();
        stubTestEndpoint();
        baseClient = createBaseClient();
        StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(baseClient.post("/test", emptyPublisher(), "1")))
                .verifyComplete();
        Mockito.verify(schedulerSpy).schedule(Mockito.any(Runnable.class), Mockito.eq(599999L), Mockito.eq(TimeUnit.MILLISECONDS));
    }

    @Test
    public void should_retry_if_server_error() {
        stubTokenEndpoint(serverError());
        stubTestEndpoint();
        baseClient = createBaseClient();
        assertThatThrownBy(() -> baseClient.post("/test", emptyPublisher(), "1")).isInstanceOf(IllegalStateException.class);
        //Added delay because it fails sometimes
        Mockito.verify(schedulerSpy, Mockito.after(1000)).schedule(Mockito.any(Runnable.class), Mockito.eq(30L), Mockito.eq(TimeUnit.SECONDS));
    }

    @Test
    public void should_not_retry_if_client_error() {
        stubTokenEndpoint(forbidden());
        stubTestEndpoint();
        baseClient = createBaseClient();
        assertThatThrownBy(() -> baseClient.post("/test", emptyPublisher(), "1")).isInstanceOf(IllegalStateException.class);
        Mockito.verify(schedulerSpy, Mockito.atMostOnce()).schedule(Mockito.any(Runnable.class), Mockito.anyLong(), Mockito.any());
    }

    @Test
    public void should_handle_connection_reset() {
        stubTokenEndpoint(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER));
        stubTestEndpoint();
        baseClient = createBaseClient();
        assertThatThrownBy(() -> baseClient.post("/test", emptyPublisher(), "1")).isInstanceOf(IllegalStateException.class);
        Mockito.verify(schedulerSpy, Mockito.atMostOnce()).schedule(Mockito.any(Runnable.class), Mockito.anyLong(), Mockito.any());
    }

    @Test
    public void should_handle_delay() throws ExecutionException, InterruptedException, TimeoutException {
        stubTokenEndpoint(validTokenResponse().withFixedDelay(2000));
        stubTestEndpoint();
        baseClient = createBaseClient();
        baseClient.post("/test", null, "1");
        StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(baseClient.post("/test", emptyPublisher(), "1")))
                .verifyComplete();
        Mockito.verify(schedulerSpy).schedule(Mockito.any(Runnable.class), Mockito.eq(599999L), Mockito.eq(TimeUnit.MILLISECONDS));
    }

    private BaseClient createBaseClient() {
        return new BaseClient("http://localhost:8443", "key", "username", "password", clock, schedulerSpy);
    }

    private StubMapping stubTestEndpoint() {
        return stubFor(post("/test")
                .withHeader("Authorization", new EqualToPattern("Bearer a-token"))
                .withHeader("X-Correlation-Id", new EqualToPattern("1"))
                .willReturn(ok()));
    }

    private StubMapping stubTokenEndpoint() {
        return stubFor(post("/token")
                .withHeader("Ocp-Apim-Subscription-Key", new EqualToPattern("key"))
                .withBasicAuth("username", "password")
                .willReturn(validTokenResponse()));
    }

    private ResponseDefinitionBuilder validTokenResponse() {
        return ok().withBody(tokenResponseFromFile);
    }

    private StubMapping stubTokenEndpoint(ResponseDefinitionBuilder responseBuilder) {
        return stubFor(post("/token")
                .withHeader("Ocp-Apim-Subscription-Key", new EqualToPattern("key"))
                .withBasicAuth("username", "password")
                .willReturn(responseBuilder));
    }

}