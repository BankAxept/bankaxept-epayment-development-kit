package no.bankaxept.epayment.webflux.client.tokenrequestor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import no.bankaxept.epayment.client.base.BaseClient;
import no.bankaxept.epayment.client.base.MapOperator;
import no.bankaxept.epayment.client.base.RequestStatus;
import no.bankaxept.epayment.client.base.SimulationRequest;
import no.bankaxept.epayment.client.base.SinglePublisher;
import no.bankaxept.epayment.client.base.http.HttpResponse;
import no.bankaxept.epayment.webflux.client.tokenrequestor.bankaxept.EligibilityRequest;
import no.bankaxept.epayment.webflux.client.tokenrequestor.bankaxept.EnrolCardRequest;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.publisher.Mono;

public class TokenRequestorClient {

  private final static String SIMULATION_HEADER = "X-Simulation";
  private final BaseClient baseClient;
  private final ObjectMapper objectMapper = new ObjectMapper()
      .registerModule(new JavaTimeModule())
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  private final Executor executor = Executors.newSingleThreadExecutor();

  public TokenRequestorClient(BaseClient baseClient) {
    this.baseClient = baseClient;
  }

  public TokenRequestorClient(URL authorizationServerUrl, URL resourceServerUrl, String clientId, String clientSecret) {
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

  public Mono<RequestStatus> enrolCard(EnrolCardRequest request, String correlationId) {
    return JdkFlowAdapter.flowPublisherToFlux(new MapOperator<>(
        baseClient.post(
            "/v1/payment-tokens",
            new SinglePublisher<>(json(request), executor),
            correlationId,
            findSimulationHeader(request)
        ),
        HttpResponse::requestStatus
    )).single();
  }

  public Mono<RequestStatus> deleteToken(String tokenId, String correlationId) {
    return JdkFlowAdapter.flowPublisherToFlux(new MapOperator<>(
        baseClient.post(
            String.format("/v1/payment-tokens/%s/deletion", tokenId),
            new SinglePublisher<>("", executor),
            correlationId
        ),
        HttpResponse::requestStatus
    )).single();
  }

  public Mono<RequestStatus> eligibleBanks(List<String> bankIdentifiers) {
    return JdkFlowAdapter.flowPublisherToFlux(new MapOperator<>(
        baseClient.get(
            "/v1/eligible-banks?bankIdentifier=" + String.join(",", bankIdentifiers),
            Map.of()
        ),
        HttpResponse::requestStatus
    )).single();
  }

  public Mono<RequestStatus> cardEligibility(EligibilityRequest request, String correlationId) {
    return JdkFlowAdapter.flowPublisherToFlux(new MapOperator<>(
        baseClient.post(
            "/v1/card-eligibility",
            new SinglePublisher<>(json(request), executor),
            correlationId
        ),
        HttpResponse::requestStatus
    )).single();
  }

  private <T> String json(T input) {
    try {
      return objectMapper.writeValueAsString(input);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
