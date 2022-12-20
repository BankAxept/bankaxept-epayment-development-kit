package no.bankaxept.epayment.test.client.base;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.matching.ContentPattern;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import no.bankaxept.epayment.client.base.accesstoken.AccessTokenPublisher;
import no.bankaxept.epayment.client.base.accesstoken.AccessTokenSubscriber;
import no.bankaxept.epayment.client.base.accesstoken.ScheduledAccessTokenPublisher;
import no.bankaxept.epayment.client.webflux.WebFluxClient;
import no.bankaxept.epayment.test.client.AbstractWireMockTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import reactor.adapter.JdkFlowAdapter;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AccessTokenWireMockTest extends AbstractWireMockTest {

    private AccessTokenPublisher accessTokenProcessor;

    @AfterEach
    public void tearDown() {
        accessTokenProcessor.shutDown();
    }

    @Test
    public void should_schedule_on_startup_then_new_on_success(WireMockRuntimeInfo wmRuntimeInfo) throws IOException {
        stubFor(tokenEndpointMapping(validTokenResponse()).withRequestBody(new EqualToPattern(readFromFile("authentication.request"))));
        accessTokenProcessor = new ScheduledAccessTokenPublisher("bankaxept-epayment/access-token-api/v1/accesstoken",
                "username",
                "password",
                "read",
                "grant",
                new WebFluxClient("http://localhost:" + wmRuntimeInfo.getHttpPort()),
                clock);
        assertEquals("a-token", new AccessTokenSubscriber(accessTokenProcessor).get(Duration.of(10, ChronoUnit.SECONDS)));

    }


    private String readFromFile(@SuppressWarnings("SameParameterValue") String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource(filename)).getPath())));
    }
}