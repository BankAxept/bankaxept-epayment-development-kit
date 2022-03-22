package no.bankaxept.epayment.client.merchant;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import no.bankaxept.client.test.AbstractBaseClientWireMockTest;
import no.bankaxept.epayment.swagger.merchant.api.Amount;
import no.bankaxept.epayment.swagger.merchant.api.PaymentRequest;
import org.junit.jupiter.api.Test;
import reactor.adapter.JdkFlowAdapter;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class MerchantClientTest extends AbstractBaseClientWireMockTest {
    private MerchantClient client;

    @Test
    public void simple_payment_request() throws JsonProcessingException {
        var transactionTime = OffsetDateTime.now();
        stubFor(PaymentEndpointMapping(transactionTime));
        client = new MerchantClient(baseClient);
        var paymentRequest = createPaymentRequest(transactionTime);
        StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(client.payment(paymentRequest, "1")))
                .verifyComplete();
    }

    private PaymentRequest createPaymentRequest(OffsetDateTime transactionTime) {
        return new PaymentRequest()
                .amount(new Amount().currency("NOK").value(10000L))
                .merchantId("10030005")
                .merchantName("Corner shop")
                .merchantReference("reference")
                .messageId("74313af1-e2cc-403f-85f1-6050725b01b6")
                .inStore(true)
                .transactionTime(transactionTime);
    }

    private MappingBuilder PaymentEndpointMapping(OffsetDateTime transactionTime) {
        return post("/payments")
                .withHeader("Authorization", new EqualToPattern("Bearer a-token"))
                .withHeader("X-Correlation-Id", new EqualToPattern("1"))
                .withRequestBody(matchingJsonPath("merchantId", equalTo("10030005")))
                .withRequestBody(matchingJsonPath("merchantName", equalTo("Corner shop")))
                .withRequestBody(matchingJsonPath("merchantReference", equalTo("reference")))
                .withRequestBody(matchingJsonPath("messageId", equalTo("74313af1-e2cc-403f-85f1-6050725b01b6")))
                .withRequestBody(matchingJsonPath("inStore", equalTo("true")))
                .withRequestBody(matchingJsonPath("amount", containing("10000").and(containing("NOK"))))
                .withRequestBody(matchingJsonPath("transactionTime", equalTo(transactionTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))))
                .willReturn(ok());
    }

}