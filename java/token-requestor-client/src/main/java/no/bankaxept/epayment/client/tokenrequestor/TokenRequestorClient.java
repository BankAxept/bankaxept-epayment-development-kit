package no.bankaxept.epayment.client.tokenrequestor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.bankaxept.epayment.client.base.*;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;

public class TokenRequestorClient {
    private final BaseClient baseClient;

    private final static String ENROLMENT_URL = "/payment-tokens";
    private final static String DELETION_URL = "/payment-tokens/%s/deletion"; //Token id in path

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final Executor executor = Executors.newSingleThreadExecutor();

    public TokenRequestorClient(BaseClient baseClient) {
        this.baseClient = baseClient;
    }

    public TokenRequestorClient(String baseurl, String apimKey, String username, String password) {
        this.baseClient = new BaseClient(baseurl, apimKey, username, password);
    }

    public Flow.Publisher<RequestResponse<EnrolCardResponse>> enrol(EnrolCardRequest request, String correlationId) {
        return new MapOperator<>(baseClient.post(ENROLMENT_URL, new SinglePublisher<>(serialize(request), executor), correlationId),
                httpResponse -> new RequestResponse<>(httpResponse.getStatus().toResponse(),
                        deserializeOrNull(EnrolCardResponse.class, httpResponse.getBody())));
    }

    public Flow.Publisher<RequestStatus> delete(String tokenId, String correlationId) {
        return new MapOperator<>(baseClient.post(String.format(DELETION_URL, tokenId), new SinglePublisher<>("", executor), correlationId),
                httpResponse -> httpResponse.getStatus().toResponse());
    }

    private <T> String serialize(T input) {
        try {
            return objectMapper.writeValueAsString(input);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T deserializeOrNull(Class<T> clazz, String input) {
        try {
            return objectMapper.readValue(input, clazz);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
