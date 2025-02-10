package no.bankaxept.epayment.client.wallet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import no.bankaxept.epayment.client.base.BaseClient;
import no.bankaxept.epayment.client.base.MapOperator;
import no.bankaxept.epayment.client.base.RequestStatus;
import no.bankaxept.epayment.client.base.SinglePublisher;
import no.bankaxept.epayment.client.base.http.HttpResponse;
import no.bankaxept.epayment.client.wallet.bankaxept.EnrolCardRequest;
import no.bankaxept.epayment.client.wallet.bankaxept.PaymentRequest;

public class WalletClient {

  private final BaseClient baseClient;

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule())
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  private final Executor executor = Executors.newSingleThreadExecutor();

  public WalletClient(BaseClient baseClient) {
    this.baseClient = baseClient;
  }

  public WalletClient(
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

  public Flow.Publisher<RequestStatus> enrolCard(EnrolCardRequest request, String correlationId) {
    return new MapOperator<>(
        baseClient.post(
            "/wallet/v1/payment-tokens",
            new SinglePublisher<>(json(request), executor),
            correlationId
        ), HttpResponse::requestStatus
    );
  }

  public Flow.Publisher<RequestStatus> deleteToken(UUID tokenId, String correlationId) {
    return new MapOperator<>(
        baseClient.delete(String.format("/wallet/v1/payment-tokens/%s", tokenId), correlationId),
        HttpResponse::requestStatus
    );
  }

  public Flow.Publisher<RequestStatus> requestPayment(PaymentRequest request, String correlationId) {
    return new MapOperator<>(
        baseClient.post(
            "/wallet/v1/payments",
            new SinglePublisher<>(json(request), executor),
            correlationId
        ),
        HttpResponse::requestStatus
    );
  }

  private <T> String json(T input) {
    try {
      return objectMapper.writeValueAsString(input);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

}
