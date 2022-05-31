package no.bankaxept.epayment.test.client.tokenrequestor;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import no.bankaxept.epayment.test.client.AbstractBaseClientWireMockTest;
import no.bankaxept.epayment.client.base.RequestStatus;
import no.bankaxept.epayment.client.tokenrequestor.EnrolCardRequest;
import no.bankaxept.epayment.client.tokenrequestor.TokenRequestorClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.adapter.JdkFlowAdapter;
import reactor.test.StepVerifier;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;

public class TokenRequestorClientTest extends AbstractBaseClientWireMockTest {

    private TokenRequestorClient client;

    //Examples from openapi definition
    private static final String encryptedExampleData = "eyJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiYWxnIjoiUlNBLU9BRVAtMjU2In0.f5nkE6FuGYkoa4usRQ1MhUJY34pYi31xgSiApiR1uP4tSXV3DNnY3N5Zq9Bnt1OucN2nJxAqCcND4G8TpGw9kofFcLcs5kXHg7nmIgjI8ZXTYx7GuZ_w6YxVTzCmjT5dpSlOQFkuCfJn2VdKnF4PjaqiKW9fWluOKorUZdsjsDl5PjIjf3ndqCtGEma6TBpKxLX0FnCZzvsVATCBcxqwKLvkAYFdFFtLfxe5OvW0PFsy4OjasODW3Kk55e58v5xXB8bP9hzr5S7sXFlzX2TG583MLLXG3K1E3XG0R262vs2cGgSA1B6zmujvmkpR4lLofwgahpO-ZrhGZtXE0-wFJw.NDB8Ln7XCf1q1p6ddRvnSw.PTBsKUkN5stmSQwrQ-jQLA.ece3W1q3AiMdg5QQbAd1tq_nQWLRkyNnk2mL1TP8fpQ";
    private static final String messageIdExample = "74313af1-e2cc-403f-85f1-6050725b01b6";
    private static final String tokenRequestorIdExample = "19474920408";
    private static final String someCorrelationId = "1";

    private final UUID tokenId = UUID.randomUUID();

    @BeforeEach
    public void setup(WireMockRuntimeInfo wmRuntimeInfo) {
        super.setup(wmRuntimeInfo);
        client = new TokenRequestorClient(baseClient);
    }

    @Test
    public void enrolment_successful() {
        stubFor(EnrolmentEndpoint(created()));
        StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(client.enrol(createEnrolmentRequest(), someCorrelationId)))
                .expectNext(RequestStatus.Accepted)
                .verifyComplete();
    }

    @Test
    public void deletion_successful() {
        stubFor(DeletionEndpoint(created()));
        StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(client.delete(tokenId.toString(), someCorrelationId)))
                .expectNext(RequestStatus.Accepted)
                .verifyComplete();
    }


    private EnrolCardRequest createEnrolmentRequest() {
        return new EnrolCardRequest()
                .tokenRequestorId(tokenRequestorIdExample)
                .messageId(messageIdExample)
                .encryptedCardholderAuthenticationData(encryptedExampleData);

    }

    private MappingBuilder DeletionEndpoint(ResponseDefinitionBuilder responseBuilder) {
        return post("/payment-tokens/" + tokenId + "/deletion")
                .withHeader("Authorization", new EqualToPattern(bearerToken()))
                .withHeader("X-Correlation-Id", new EqualToPattern(someCorrelationId))
                .willReturn(responseBuilder);
    }

    private MappingBuilder EnrolmentEndpoint(ResponseDefinitionBuilder responseBuilder) {
        return post("/payment-tokens")
                .withHeader("Authorization", new EqualToPattern(bearerToken()))
                .withHeader("X-Correlation-Id", new EqualToPattern(someCorrelationId))
                .withRequestBody(matchingJsonPath("tokenRequestorId", equalTo(tokenRequestorIdExample)))
                .withRequestBody(matchingJsonPath("messageId", equalTo(messageIdExample)))
                .withRequestBody(matchingJsonPath("encryptedCardholderAuthenticationData", equalTo(encryptedExampleData)))
                .willReturn(responseBuilder);
    }

}