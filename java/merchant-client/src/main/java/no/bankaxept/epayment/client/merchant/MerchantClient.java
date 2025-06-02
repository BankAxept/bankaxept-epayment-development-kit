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
import no.bankaxept.epayment.client.base.RequestStatus;
import no.bankaxept.epayment.client.base.SimulationRequest;
import no.bankaxept.epayment.client.base.SinglePublisher;
import no.bankaxept.epayment.client.base.http.HttpResponse;
import reactor.core.publisher.Mono;

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
      String clientId,
      String clientSecret
  ) {
    this(
        new BaseClient.Builder(resourceServerUrl)
            .withScheduledToken(authorizationServerUrl, clientId, clientSecret)
            .build()
    );
  }

  private static Map<String, List<String>> findSimulationHeader(Object request) {
    if (request instanceof SimulationRequest) {
      return Map.of(SIMULATION_HEADER, ((SimulationRequest) request).getSimulationValues());
    }
    return Map.of();
  }

  public Mono<RequestStatus> requestPayment(PaymentRequest request, String correlationId) {
    return baseClient.post(
            "/v1/payments",
            new SinglePublisher<>(json(request), executor),
            correlationId,
            findSimulationHeader(request)
        ).map(HttpResponse::requestStatus);
  }

  public Mono<RequestStatus> rollbackPayment(String correlationId, String messageId) {
    return baseClient.delete(String.format("/v1/payments/messages/%s", messageId), correlationId).map(HttpResponse::requestStatus);
  }

  public Mono<RequestStatus> capturePayment(String paymentId, CaptureRequest request, String correlationId) {
    return baseClient.post(
            String.format("/v1/payments/%s/captures", paymentId),
            new SinglePublisher<>(json(request), executor),
            correlationId,
            findSimulationHeader(request)
        ).map(HttpResponse::requestStatus);
  }

  public Mono<RequestStatus> cancelPayment(String paymentId, CancellationRequest request, String correlationId) {
    return baseClient.post(
            String.format("/v1/payments/%s/cancellation", paymentId),
            new SinglePublisher<>(json(request), executor),
            correlationId,
            findSimulationHeader(request)
        ).map(HttpResponse::requestStatus);
  }

  public Mono<RequestStatus> refundPayment(String paymentId, RefundRequest request, String correlationId) {
    return baseClient.post(
            String.format("/v1/payments/%s/refunds", paymentId),
            new SinglePublisher<>(json(request), executor),
            correlationId,
            findSimulationHeader(request)
    ).map(HttpResponse::requestStatus);
  }

  public Mono<RequestStatus> cutOff(
      String merchantId, CutOffRequest request,
      String batchNumber, String correlationId
  ) {
    return baseClient.put(
            String.format("/v1/settlements/%s/%s", merchantId, batchNumber),
            new SinglePublisher<>(json(request), executor),
            correlationId
        ).map(HttpResponse::requestStatus);
  }

  public Mono<RequestStatus> rollbackRefund(String paymentId, String messageId, String correlationId) {
    return baseClient.delete(
            String.format("/v1/payments/%s/refunds/messages/%s", paymentId, messageId),
            correlationId
        ).map(HttpResponse::requestStatus);
  }

  private <T> String json(T request) {
    try {
      return objectMapper.writeValueAsString(request);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

}
