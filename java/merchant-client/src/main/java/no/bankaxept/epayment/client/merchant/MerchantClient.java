package no.bankaxept.epayment.client.merchant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import no.bankaxept.epayment.client.base.BaseClient;
import no.bankaxept.epayment.client.base.MapOperator;
import no.bankaxept.epayment.client.base.RequestStatus;
import no.bankaxept.epayment.client.base.SimulationRequest;
import no.bankaxept.epayment.client.base.SinglePublisher;

public class MerchantClient {

  private final BaseClient baseClient;

  private final static String SIMULATION_HEADER = "X-Simulation";

  private final ObjectMapper objectMapper = new ObjectMapper()
      .registerModule(new JavaTimeModule())
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  private final Executor executor = Executors.newSingleThreadExecutor();

  public MerchantClient(BaseClient baseClient) {
    this.baseClient = baseClient;
  }

  public MerchantClient(
      URL authorizationServerUrl,
      URL resourceServerUrl,
      String apimKey,
      String clientId,
      String clientSecret
  ) {
    this(
        new BaseClient.Builder(resourceServerUrl.toString())
            .apimKey(apimKey)
            .withScheduledToken(authorizationServerUrl.toString(), clientId, clientSecret)
            .build()
    );
  }

  public MerchantClient(URL authorizationServerUrl, URL merchantServerUrl, String clientId, String clientSecret) {
    this(authorizationServerUrl, merchantServerUrl, null, clientId, clientSecret);
  }

  private static Map<String, List<String>> findSimulationHeader(Object request) {
    if (request instanceof SimulationRequest) {
      return Map.of(SIMULATION_HEADER, ((SimulationRequest) request).getSimulationValues());
    }
    return Map.of();
  }

  public Flow.Publisher<RequestStatus> requestPayment(PaymentRequest request, String correlationId) {
    try {
      return new MapOperator<>(baseClient.post(
          "v1/payments",
          new SinglePublisher<>(objectMapper.writeValueAsString(request), executor),
          correlationId,
          findSimulationHeader(request)
      ), httpResponse -> httpResponse.getStatus().toResponse());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public Flow.Publisher<RequestStatus> rollbackPayment(String correlationId, String messageId) {
    return new MapOperator<>(
        baseClient.delete(String.format("v1/payments/messages/%s", messageId), correlationId),
        httpResponse -> httpResponse.getStatus().toResponse()
    );
  }

  public Flow.Publisher<RequestStatus> capturePayment(String paymentId, CaptureRequest request, String correlationId) {
    try {
      return new MapOperator<>(baseClient.post(
          String.format("v1/payments/%s/captures", paymentId),
          new SinglePublisher<>(objectMapper.writeValueAsString(request), executor),
          correlationId,
          findSimulationHeader(request)
      ), httpResponse -> httpResponse.getStatus().toResponse());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public Flow.Publisher<RequestStatus> cancelPayment(String paymentId, String correlationId) {
    return new MapOperator<>(baseClient.post(
        String.format("v1/payments/%s/cancellation", paymentId),
        new SinglePublisher<>("", executor),
        correlationId
    ), httpResponse -> httpResponse.getStatus().toResponse());
  }

  public Flow.Publisher<RequestStatus> refundPayment(String paymentId, RefundRequest request, String correlationId) {
    try {
      return new MapOperator<>(baseClient.post(
          String.format("v1/payments/%s/refunds", paymentId),
          new SinglePublisher<>(objectMapper.writeValueAsString(request), executor),
          correlationId,
          findSimulationHeader(request)
      ), httpResponse -> httpResponse.getStatus().toResponse());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public Flow.Publisher<RequestStatus> cutOff(
      String merchantId, CutOffRequest request,
      String batchNumber, String correlationId
  ) {
    try {
      return new MapOperator<>(
          baseClient.put(
              String.format("v1/settlements/%s/%s", merchantId, batchNumber),
              new SinglePublisher<>(objectMapper.writeValueAsString(request), executor),
              correlationId
          ),
          httpResponse -> httpResponse.getStatus().toResponse()
      );
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public Flow.Publisher<RequestStatus> rollbackRefund(String paymentId, String messageId, String correlationId) {
    return new MapOperator<>(
        baseClient.delete(
            String.format("v1/payments/%s/refunds/messages/%s", paymentId, messageId),
            correlationId
        ),
        httpResponse -> httpResponse.getStatus().toResponse()
    );
  }
}
