package no.bankaxept.epayment.test.client.merchant;


import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import no.bankaxept.epayment.client.merchant.SimulationPaymentRequest;
import no.bankaxept.epayment.test.client.AbstractBaseClientWireMockTest;
import no.bankaxept.epayment.client.base.RequestStatus;
import no.bankaxept.epayment.client.merchant.Amount;
import no.bankaxept.epayment.client.merchant.MerchantClient;
import no.bankaxept.epayment.client.merchant.PaymentRequest;
import org.junit.jupiter.api.BeforeEach;
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
    public void setup(WireMockRuntimeInfo wmRuntimeInfo) {
        super.setup(wmRuntimeInfo);
        client = new MerchantClient(baseClient);
    }

    @Test
    public void success() {
        stubFor(paymentEndpointMapping(transactionTime, created()));
        StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(client.payment(createPaymentRequest(transactionTime), "1")))
                .expectNext(RequestStatus.Accepted)
                .verifyComplete();
    }

    @Test
    public void success_with_simulation() {
        stubFor(simulationPaymentEndpointMapping(transactionTime, created()));
        StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(client.payment(createSimulationRequest(transactionTime), "1")))
                .expectNext(RequestStatus.Accepted)
                .verifyComplete();
    }

    @Test
    public void server_error() {
        stubFor(paymentEndpointMapping(transactionTime, serverError()));
        StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(client.payment(createPaymentRequest(transactionTime), "1")))
                .expectNext(RequestStatus.Failed)
                .verifyComplete();
    }

    @Test
    public void client_error() {
        stubFor(paymentEndpointMapping(transactionTime, forbidden()));
        var paymentRequest = createPaymentRequest(transactionTime);
        StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(client.payment(paymentRequest, "1")))
                .expectNext(RequestStatus.ClientError)
                .verifyComplete();
    }

    private PaymentRequest createSimulationRequest(OffsetDateTime transactionTime) {
        return new SimulationPaymentRequest(createPaymentRequest(transactionTime))
                .simulationValue("test-value");
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

    private MappingBuilder simulationPaymentEndpointMapping(OffsetDateTime transactionTime, ResponseDefinitionBuilder responseBuilder) {
        return paymentMapping(transactionTime)
                .withHeader("X-Simulation", new EqualToPattern("test-value"))
                .willReturn(responseBuilder);
    }

    private MappingBuilder paymentEndpointMapping(OffsetDateTime transactionTime, ResponseDefinitionBuilder responseBuilder) {
        return paymentMapping(transactionTime)
                .willReturn(responseBuilder);
    }

    private MappingBuilder paymentMapping(OffsetDateTime transactionTime) {
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
                .withRequestBody(notMatching("^(.*)simulationValues(.*)$"));

    }
}