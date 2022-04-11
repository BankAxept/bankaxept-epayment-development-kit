package no.bankaxept.client.test;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import no.bankaxept.epayment.client.base.BaseClient;
import no.bankaxept.epayment.client.base.SinglePublisher;
import org.junit.jupiter.api.BeforeEach;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;

@BaseClientWireMockTest
public abstract class AbstractBaseClientWireMockTest {

    protected BaseClient baseClient; //Because it fetches token on start, it needs to be started after setting up wiremock
    private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
    private final static String aToken = "a-token";

    private final String validTokenResponse = "{\n" +
            "\"expiresOn\": " + clock.instant().plus(2, ChronoUnit.HOURS).toEpochMilli() + ",\n" +
            "\"accessToken\": \"" + aToken +"\"\n" +
            "}";
    private final Executor executor = Executors.newSingleThreadExecutor();

    @BeforeEach
    public void setup() {
        WireMock.stubFor(tokenEndpointMapping(validTokenResponse()));
        baseClient = createBaseClient();
    }

    protected MappingBuilder tokenEndpointMapping(ResponseDefinitionBuilder responseBuilder) {
        return WireMock.post("/token")
                .withHeader("Ocp-Apim-Subscription-Key", new EqualToPattern("key"))
                .withBasicAuth("username", "password")
                .willReturn(responseBuilder);
    }

    protected ResponseDefinitionBuilder validTokenResponse() {
        return WireMock.ok().withBody(validTokenResponse);
    }

    protected BaseClient createBaseClient() {
        return new BaseClient("http://localhost:8443", "key", "username", "password", clock);
    }

    protected Flow.Publisher<String> emptyPublisher() {
        return new SinglePublisher<>("", executor);
    }

    protected String bearerToken() {
        return "Bearer " + aToken;
    }

}
