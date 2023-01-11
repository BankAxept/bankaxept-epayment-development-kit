package no.bankaxept.epayment.test.client.base;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import no.bankaxept.epayment.client.accesstoken.AccessTokenRetriever;
import no.bankaxept.epayment.client.base.accesstoken.GrantType;
import no.bankaxept.epayment.client.base.accesstoken.Scope;
import no.bankaxept.epayment.test.client.AbstractWireMockTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AccessTokenWireMockTest extends AbstractWireMockTest {

    private AccessTokenRetriever tokenRetriever;

    @Test
    public void should_schedule_on_startup_then_new_on_success(WireMockRuntimeInfo wmRuntimeInfo) throws IOException {
        stubFor(tokenEndpointWithoutApimMapping(validTokenResponse()).withRequestBody( new EqualToPattern(readFromFile("authentication.request"))));
        tokenRetriever = new AccessTokenRetriever (
                "http://localhost:" + wmRuntimeInfo.getHttpPort() + "/bankaxept-epayment/access-token-api/v1/accesstoken",
                "username",
                "password",
                GrantType.client_credentials,
                Scope.BankIDScope.openid);
        assertEquals("a-token", tokenRetriever.get());

    }


    private String readFromFile(@SuppressWarnings("SameParameterValue") String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource(filename)).getPath())));
    }
}
