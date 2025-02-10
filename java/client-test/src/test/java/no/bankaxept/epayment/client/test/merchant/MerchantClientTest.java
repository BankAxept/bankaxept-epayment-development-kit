package no.bankaxept.epayment.client.test.merchant;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.created;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.forbidden;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.notMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static no.bankaxept.epayment.client.test.Verifier.verifyForbiddenRequest;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import java.net.MalformedURLException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import no.bankaxept.epayment.client.base.RequestStatus;
import no.bankaxept.epayment.client.merchant.Amount;
import no.bankaxept.epayment.client.merchant.CaptureRequest;
import no.bankaxept.epayment.client.merchant.CutOffRequest;
import no.bankaxept.epayment.client.merchant.MerchantClient;
import no.bankaxept.epayment.client.merchant.PaymentRequest;
import no.bankaxept.epayment.client.merchant.RefundRequest;
import no.bankaxept.epayment.client.merchant.SimulationPaymentRequest;
import no.bankaxept.epayment.client.test.AbstractBaseClientWireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import reactor.adapter.JdkFlowAdapter;
import reactor.test.StepVerifier;

public class MerchantClientTest extends AbstractBaseClientWireMockTest {

  private MerchantClient client;
  private final OffsetDateTime transactionTime = OffsetDateTime.now();

  @BeforeEach
  public void setup(WireMockRuntimeInfo wmRuntimeInfo) throws MalformedURLException {
    super.setup(wmRuntimeInfo);
    client = new MerchantClient(baseClient);
  }

  @Nested
  @DisplayName("Payment")
  public class paymentTests {

    @Test
    public void success() {
      stubFor(paymentEndpointMapping(transactionTime, created()));
      StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(client.requestPayment(
              createPaymentRequest(transactionTime),
              "1"
          )))
          .expectNext(RequestStatus.Accepted)
          .verifyComplete();
    }

    @Test
    public void success_with_simulation() {
      stubFor(simulationPaymentEndpointMapping(transactionTime, created()));
      StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(client.requestPayment(
              createSimulationRequest(transactionTime),
              "1"
          )))
          .expectNext(RequestStatus.Accepted)
          .verifyComplete();
    }


    @Test
    public void server_error() {
      stubFor(paymentEndpointMapping(transactionTime, serverError()));
      StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(client.requestPayment(
              createPaymentRequest(transactionTime),
              "1"
          )))
          .expectNext(RequestStatus.Failed)
          .verifyComplete();
    }

    @Test
    public void client_error() {
      stubFor(paymentEndpointMapping(transactionTime, forbidden()));
      var paymentRequest = createPaymentRequest(transactionTime);
      verifyForbiddenRequest(client.requestPayment(paymentRequest, "1"));
    }

    private PaymentRequest createSimulationRequest(OffsetDateTime transactionTime) {
      return new SimulationPaymentRequest()
          .simulationValue("test-value")
          .amount(new Amount().currency("NOK").value(10000L))
          .merchantId("10030005")
          .merchantName("Corner shop")
          .merchantReference("reference")
          .messageId("74313af1-e2cc-403f-85f1-6050725b01b6")
          .inStore(true)
          .transactionTime(transactionTime);
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

    private MappingBuilder paymentEndpointMapping(
        OffsetDateTime transactionTime,
        ResponseDefinitionBuilder responseBuilder
    ) {
      return paymentMapping(transactionTime)
          .willReturn(responseBuilder);
    }

    private MappingBuilder paymentMapping(OffsetDateTime transactionTime) {
      return post(urlPathEqualTo("/merchant/v1/payments"))
          .withHeader("Authorization", new EqualToPattern("Bearer a-token"))
          .withHeader("X-Correlation-Id", new EqualToPattern("1"))
          .withRequestBody(matchingJsonPath("merchantId", equalTo("10030005")))
          .withRequestBody(matchingJsonPath("merchantName", equalTo("Corner shop")))
          .withRequestBody(matchingJsonPath("merchantReference", equalTo("reference")))
          .withRequestBody(matchingJsonPath("messageId", equalTo("74313af1-e2cc-403f-85f1-6050725b01b6")))
          .withRequestBody(matchingJsonPath("inStore", equalTo("true")))
          .withRequestBody(matchingJsonPath("amount", containing("10000").and(containing("NOK"))))
          .withRequestBody(matchingJsonPath(
              "transactionTime",
              equalTo(transactionTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
          ))
          .withRequestBody(notMatching("^(.*)simulationValues(.*)$"));

    }

    private MappingBuilder simulationPaymentEndpointMapping(
        OffsetDateTime transactionTime,
        ResponseDefinitionBuilder responseBuilder
    ) {
      return paymentMapping(transactionTime)
          .withHeader("X-Simulation", new EqualToPattern("test-value"))
          .willReturn(responseBuilder);
    }
  }

  @Nested
  @DisplayName("Payment Rollback")
  public class RollbackTest {

    @Test
    public void success() {
      stubFor(PaymentRollbackEndpointMapping("1", "message-id", created()));
      StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(client.rollbackPayment("1", "message-id")))
          .expectNext(RequestStatus.Accepted)
          .verifyComplete();
    }

    private MappingBuilder PaymentRollbackEndpointMapping(
        String correlationId,
        String messageId,
        ResponseDefinitionBuilder responseBuilder
    ) {
      return delete(urlPathEqualTo(String.format("/merchant/v1/payments/messages/%s", messageId)))
          .withHeader("Authorization", new EqualToPattern("Bearer a-token"))
          .withHeader("X-Correlation-Id", new EqualToPattern(correlationId))
          .willReturn(responseBuilder);
    }
  }


  @Nested
  @DisplayName("Capture")
  public class CaptureTest {

    @Test
    public void success() {
      stubFor(CaptureEndpointMapping("payment-id", "1", created()));
      StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(client.capturePayment(
              "payment-id",
              new CaptureRequest().amount(new Amount().currency("NOK").value(10000L))
                  .messageId("74313af1-e2cc-403f-85f1-6050725b01b6"),
              "1"
          )))
          .expectNext(RequestStatus.Accepted)
          .verifyComplete();
    }

    private MappingBuilder CaptureEndpointMapping(
        String paymentId,
        String correlationId,
        ResponseDefinitionBuilder responseBuilder
    ) {
      return post(urlPathEqualTo(String.format("/merchant/v1/payments/%s/captures", paymentId)))
          .withHeader("Authorization", new EqualToPattern("Bearer a-token"))
          .withHeader("X-Correlation-Id", new EqualToPattern(correlationId))
          .withRequestBody(matchingJsonPath("messageId", equalTo("74313af1-e2cc-403f-85f1-6050725b01b6")))
          .withRequestBody(matchingJsonPath("amount", containing("10000").and(containing("NOK"))))
          .willReturn(responseBuilder);
    }
  }

  @Nested
  @DisplayName("Cancel")
  public class CancelTest {

    @Test
    public void success() {
      stubFor(CancelEndpointMapping("payment-id", "1", created()));
      StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(client.cancelPayment("payment-id", "1")))
          .expectNext(RequestStatus.Accepted)
          .verifyComplete();
    }

    private MappingBuilder CancelEndpointMapping(
        String paymentId,
        String correlationId,
        ResponseDefinitionBuilder responseBuilder
    ) {
      return post(urlPathEqualTo(String.format("/merchant/v1/payments/%s/cancellation", paymentId)))
          .withHeader("Authorization", new EqualToPattern("Bearer a-token"))
          .withHeader("X-Correlation-Id", new EqualToPattern(correlationId))
          .willReturn(responseBuilder);
    }

    @Nested
    @DisplayName("Refund")
    public class RefundTest {

      @Test
      public void success() {
        stubFor(RefundEndpointMapping(
            "payment-id",
            "1",
            created()
        ));
        StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(client.refundPayment(
                "payment-id",
                new RefundRequest()
                    .amount(new Amount()
                        .value(10000L)
                        .currency("NOK"))
                    .inStore(true)
                    .messageId("74313af1-e2cc-403f-85f1-6050725b01b6")
                    .transactionTime(transactionTime),
                "1"
            )))
            .expectNext(RequestStatus.Accepted)
            .verifyComplete();
      }

      private MappingBuilder RefundEndpointMapping(
          String paymentId,
          String correlationId,
          ResponseDefinitionBuilder responseBuilder
      ) {
        return post(urlPathEqualTo(String.format("/merchant/v1/payments/%s/refunds", paymentId)))
            .withHeader("Authorization", new EqualToPattern("Bearer a-token"))
            .withHeader("X-Correlation-Id", new EqualToPattern(correlationId))
            .withRequestBody(matchingJsonPath("messageId", equalTo("74313af1-e2cc-403f-85f1-6050725b01b6")))
            .withRequestBody(matchingJsonPath("amount", containing("10000").and(containing("NOK"))))
            .withRequestBody(matchingJsonPath("inStore", equalTo("true")))
            .willReturn(responseBuilder);
      }
    }

    @Nested
    @DisplayName("Refund Rollback")
    public class RefundRollbackTest {

      @Test
      public void success() {
        stubFor(RefundRollbackEndpointMapping("payment-id", "message-id", "1", created()));
        StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(client.rollbackRefund("payment-id", "message-id", "1")))
            .expectNext(RequestStatus.Accepted)
            .verifyComplete();
      }

      private MappingBuilder RefundRollbackEndpointMapping(
          String paymentId,
          String messageId,
          String correlationId,
          ResponseDefinitionBuilder responseBuilder
      ) {
        return delete(urlPathEqualTo(String.format("/merchant/v1/payments/%s/refunds/messages/%s", paymentId, messageId)))
            .withHeader("Authorization", new EqualToPattern("Bearer a-token"))
            .withHeader("X-Correlation-Id", new EqualToPattern(correlationId))
            .willReturn(responseBuilder);
      }
    }

    @Nested
    @DisplayName("Cut off Settlement Batch")
    public class CutOffSettlementBatchTest {

      @Test
      public void success() {
        CutOffRequest cutOffRequest = new CutOffRequest().messageId("message-id").merchantAggregatorId("1");
        stubFor(cutOffSettlementBatchEndpointMapping("merchant-id", "batch-number", "1", created()));
        StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(
                client.cutOff("merchant-id", cutOffRequest, "batch-number", "1")))
            .expectNext(RequestStatus.Accepted)
            .verifyComplete();
      }

      private MappingBuilder cutOffSettlementBatchEndpointMapping(
          String merchantId,
          String batchNumber,
          String correlationId,
          ResponseDefinitionBuilder responseBuilder
      ) {
        return put(urlPathEqualTo(String.format("/merchant/v1/settlements/%s/%s", merchantId, batchNumber)))
            .withHeader("Authorization", new EqualToPattern("Bearer a-token"))
            .withHeader("X-Correlation-Id", new EqualToPattern(correlationId))
            .willReturn(responseBuilder);
      }
    }
  }
}
