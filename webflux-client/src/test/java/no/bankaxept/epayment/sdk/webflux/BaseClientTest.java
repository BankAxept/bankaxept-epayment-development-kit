package no.bankaxept.epayment.sdk.webflux;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import no.bankaxept.epayment.sdk.baseclient.BaseClient;
import org.junit.jupiter.api.Test;
import reactor.adapter.JdkFlowAdapter;
import reactor.test.StepVerifier;

import java.time.Instant;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@WireMockTest(httpPort = 8443)
class BaseClientTest {

    private BaseClient baseClient = new BaseClient("http://localhost:8443", "key", "username", "password");

    @Test
    public void should_add_all_relevant_headers() {
        stubFor(post("/token")
                .withHeader("Ocp-Apim-Subscription-Key", new EqualToPattern("key"))
                .withBasicAuth("username", "password")
                .willReturn(ok().withBody(
                        "{\n" +
                                "\"expiresOn\": " + Instant.now().toEpochMilli() + ",\n" +
                                "\"accessToken\": \"a-token\"\n" +
                                "}")));
        stubFor(post("/test")
                .withHeader("Authorization", new EqualToPattern("Bearer a-token"))
                .withHeader("X-Correlation-Id", new EqualToPattern("1"))
                .willReturn(ok()));
        var publisher = baseClient.post("/test", "1");
        StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(publisher))
                .verifyComplete();
    }

}