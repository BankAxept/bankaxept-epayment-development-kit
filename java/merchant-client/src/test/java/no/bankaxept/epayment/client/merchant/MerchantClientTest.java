package no.bankaxept.epayment.client.merchant;


import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import no.bankaxept.client.test.AbstractBaseClientWireMockTest;
import no.bankaxept.epayment.client.base.RequestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import reactor.adapter.JdkFlowAdapter;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class MerchantClientTest extends AbstractBaseClientWireMockTest {
    private MerchantClient client;
    private final OffsetDateTime transactionTime = OffsetDateTime.now();

    @BeforeEach
    public void setup() {
        super.setup();
        client = new MerchantClient(baseClient);
    }

    @Nested
    @DisplayName("Payments tests")
    public class PaymentsTests {

        @Test
        public void success() {
            stubFor(PaymentEndpointMapping(transactionTime, created()));
            StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(client.payment(createPaymentRequest(transactionTime), "1")))
                    .expectNext(RequestStatus.Accepted)
                    .verifyComplete();
        }

        @Test
        public void server_error() {
            stubFor(PaymentEndpointMapping(transactionTime, serverError()));
            var paymentRequest = createPaymentRequest(transactionTime);
            StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(client.payment(paymentRequest, "1")))
                    .expectNext(RequestStatus.Failed)
                    .verifyComplete();
        }

        @Test
        public void client_error() {
            stubFor(PaymentEndpointMapping(transactionTime, forbidden()));
            var paymentRequest = createPaymentRequest(transactionTime);
            StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(client.payment(paymentRequest, "1")))
                    .expectNext(RequestStatus.ClientError)
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

        private MappingBuilder PaymentEndpointMapping(OffsetDateTime transactionTime, ResponseDefinitionBuilder responseBuilder) {
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
                    .willReturn(responseBuilder);
        }
    }
}