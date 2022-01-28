package no.ebax.sdk;

import accesstoken.model.AccessTokenResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.jupiter.api.Test;
import reactor.adapter.JdkFlowAdapter;
import reactor.test.StepVerifier;

import java.sql.Date;
import java.time.Instant;
import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@WireMockTest(httpPort = 8443)
class BaseClientTest {

    private BaseClient baseClient = new BaseClient(new WebFluxClient("http://localhost:8443"), "key", "username", "password");

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void should_add_all_relevant_headers() throws JsonProcessingException {
        stubFor(post("/token")
                .withHeader("Ocp-Apim-Subscription-Key", new EqualToPattern("key"))
                .withBasicAuth("username", "password")
                .willReturn(ok().withBody(objectMapper.writeValueAsString(new AccessTokenResponse().accessToken("a-token").expiresOn(Date.from(Instant.now()).getTime())))));
        stubFor(post("/test")
                .withHeader("Authorization", new EqualToPattern("Bearer a-token"))
                .withHeader("X-Correlation-Id", new EqualToPattern("1"))
                .willReturn(ok()));
        var publisher = baseClient.post("/test",  "1");
        StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(publisher))
                .verifyComplete();
    }

}