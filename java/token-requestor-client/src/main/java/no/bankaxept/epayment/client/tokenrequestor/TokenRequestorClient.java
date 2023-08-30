package no.bankaxept.epayment.client.tokenrequestor;

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
import no.bankaxept.epayment.client.base.http.HttpResponse;
import no.bankaxept.epayment.client.tokenrequestor.bankaxept.EnrolCardRequest;

public class TokenRequestorClient {

  private final BaseClient baseClient;

  private final static String SIMULATION_HEADER = "X-Simulation";

  private final ObjectMapper objectMapper = new ObjectMapper()
      .registerModule(new JavaTimeModule())
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  private final Executor executor = Executors.newSingleThreadExecutor();

  public TokenRequestorClient(BaseClient baseClient) {
    this.baseClient = baseClient;
  }

  public TokenRequestorClient(URL authorizationServerUrl, URL resourceServerUrl, String clientId, String clientSecret) {
    this(
        new BaseClient.Builder(resourceServerUrl.toString())
            .withScheduledToken(authorizationServerUrl.toString(), clientId, clientSecret)
            .build()
    );
  }

  public Flow.Publisher<RequestStatus> enrolCard(EnrolCardRequest request, String correlationId) {
    return new MapOperator<>(
        baseClient.post(
            "/v1/payment-tokens",
            new SinglePublisher<>(json(request), executor),
            correlationId,
            findSimulationHeader(request)
        ),
        HttpResponse::requestStatus
    );
  }

  public Flow.Publisher<RequestStatus> deleteToken(String tokenId, String correlationId) {
    return new MapOperator<>(
        baseClient.post(
            String.format("/v1/payment-tokens/%s/deletion", tokenId),
            new SinglePublisher<>("", executor),
            correlationId
        ),
        HttpResponse::requestStatus
    );
  }

  private static Map<String, List<String>> findSimulationHeader(Object request) {
    if (request instanceof SimulationRequest) {
      return Map.of(SIMULATION_HEADER, ((SimulationRequest) request).getSimulationValues());
    }
    return Map.of();
  }

  private <T> String json(T input) {
    try {
      return objectMapper.writeValueAsString(input);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
