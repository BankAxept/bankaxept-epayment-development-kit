package no.bankaxept.epayment.client.wallet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import no.bankaxept.epayment.client.base.BaseClient;
import no.bankaxept.epayment.client.base.MapOperator;
import no.bankaxept.epayment.client.base.RequestStatus;
import no.bankaxept.epayment.client.base.SinglePublisher;
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

  public WalletClient(String baseurl, String apimKey, String username, String password) {
    this.baseClient = new BaseClient.Builder(baseurl).apimKey(apimKey).withScheduledToken(username, password).build();
  }

  public Flow.Publisher<RequestStatus> enrolCard(EnrolCardRequest request, String correlationId) {
    try {
      return new MapOperator<>(baseClient.post(
          "/payment-tokens",
          new SinglePublisher<>(objectMapper.writeValueAsString(request), executor),
          correlationId
      ), httpResponse -> httpResponse.getStatus().toResponse());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public Flow.Publisher<RequestStatus> deleteToken(UUID tokenId, String correlationId) {
    return new MapOperator<>(
        baseClient.delete(String.format("/payment-tokens/%s", tokenId), correlationId),
        httpResponse -> httpResponse.getStatus().toResponse()
    );
  }

  public Flow.Publisher<RequestStatus> requestPayment(PaymentRequest request, String correlationId) {
    try {
      return new MapOperator<>(baseClient.post(
          "/payments",
          new SinglePublisher<>(objectMapper.writeValueAsString(request), executor),
          correlationId
      ), httpResponse -> httpResponse.getStatus().toResponse());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

}
