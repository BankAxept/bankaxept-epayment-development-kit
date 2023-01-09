package no.bankaxept.epayment.test.client;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import no.bankaxept.epayment.client.base.SinglePublisher;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;

@BaseClientWireMockTest
public abstract class AbstractWireMockTest {

    protected final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
    protected final static String aToken = "a-token";

    private final String validTokenResponseTemplate = "{\n" +
            "\"expires_on\": " + clock.instant().plus(2, ChronoUnit.HOURS).toEpochMilli() + ",\n" +
            "\"access_token\": \"%s\"\n" +
            "}";

    private final Executor executor = Executors.newSingleThreadExecutor();

    protected MappingBuilder tokenEndpointWithoutApimMapping(ResponseDefinitionBuilder responseBuilder) {
        return WireMock.post("/bankaxept-epayment/access-token-api/v1/accesstoken")
                .withBasicAuth("username", "password")
                .willReturn(responseBuilder);
    }

    protected MappingBuilder tokenEndpointMapping(ResponseDefinitionBuilder responseBuilder) {
        return WireMock.post("/bankaxept-epayment/access-token-api/v1/accesstoken")
                .withHeader("Ocp-Apim-Subscription-Key", new EqualToPattern("key"))
                .withBasicAuth("username", "password")
                .willReturn(responseBuilder);
    }

    protected ResponseDefinitionBuilder validTokenResponse() {
        return validTokenResponse(aToken);
    }

    protected ResponseDefinitionBuilder validTokenResponse(String token) {
        return WireMock.ok().withBody(String.format(validTokenResponseTemplate, token));
    }

    protected Flow.Publisher<String> emptyPublisher() {
        return new SinglePublisher<>("", executor);
    }

    protected String bearerToken() {
        return "Bearer " + aToken;
    }

}
