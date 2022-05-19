package no.bankaxept.epayment.client.merchant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.bankaxept.epayment.client.base.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;

import static java.util.Collections.emptyMap;

public class MerchantClient {

    private final BaseClient baseClient;

    private final static String PAYMENTS_URL = "/payments";
    private final static String ROLLBACK_URL = "/payments/messages/%s";
    private final static String CAPTURE_URL = "/payments/%s/captures";


    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final Executor executor = Executors.newSingleThreadExecutor();

    public MerchantClient(BaseClient baseClient) {
        this.baseClient = baseClient;
    }

    public MerchantClient(String baseurl, String apimKey, String username, String password) {
        this.baseClient = new BaseClient(baseurl, apimKey, username, password);
    }

    public Flow.Publisher<RequestStatus> payment(PaymentRequest request, String correlationId, Map<String, List<String>> customHeaders) {
        try {
            return new MapOperator<>(baseClient.post(PAYMENTS_URL, new SinglePublisher<>(objectMapper.writeValueAsString(request), executor), correlationId, customHeaders), httpResponse -> httpResponse.getStatus().toResponse());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Flow.Publisher<RequestStatus> rollback(String correlationId, String messageId, Map<String, List<String>> customHeaders) {
        return new MapOperator<>(baseClient.delete(String.format(ROLLBACK_URL, messageId), correlationId, customHeaders), httpResponse -> httpResponse.getStatus().toResponse());
    }

    public Flow.Publisher<CaptureResponse> capture(String paymentId, CaptureRequest request, String correlationId, Map<String, List<String>> customHeaders) {
        try {
            return new MapOperator<>(baseClient.post(String.format(CAPTURE_URL, paymentId), new SinglePublisher<>(objectMapper.writeValueAsString(request), executor), correlationId, customHeaders), httpResponse -> {
                try {
                    return objectMapper.readValue(httpResponse.getBody(), CaptureResponse.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Flow.Publisher<CaptureResponse> capture(String paymentId, CaptureRequest request, String correlationId) {
        return capture(paymentId, request, correlationId, emptyMap());
    }

    public Flow.Publisher<RequestStatus> rollback(String correlationId, String messageId) {
        return rollback(correlationId, messageId, emptyMap());
    }

    public Flow.Publisher<RequestStatus> payment(PaymentRequest request, String correlationId) {
        return payment(request, correlationId, emptyMap());
    }

}
