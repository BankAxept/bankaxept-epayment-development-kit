package no.bankaxept.epayment.client.merchant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.bankaxept.epayment.client.base.*;
import no.bankaxept.epayment.client.base.http.HttpResponse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.function.Function;

public class MerchantClient {

    private final BaseClient baseClient;

    private final static String SIMULATION_HEADER = "X-Simulation";

    private final static String PAYMENTS_URL = "/payments";
    private final static String ROLLBACK_PAYMENT_URL = "/payments/messages/%s";
    private final static String CAPTURE_URL = "/payments/%s/captures";
    private final static String ROLLBACK_CAPTURE_URL = "/payments/%s/captures/messages/%s";
    private final static String CANCEL_URL = "/payments/%s/cancellation";
    private final static String REFUND_URL = "/payments/%s/refunds";
    private final static String ROLLBACK_REFUND_URL = "/payments/%s/refunds/messages/%s";
    private final static String SETTLEMENT_CUTOFF_URL = "/settlements/%s/%s";


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

    private static Map<String, List<String>> findSimulationHeader(Object request) {
        if (request instanceof SimulationRequest) {
            return Map.of(SIMULATION_HEADER, ((SimulationRequest) request).getSimulationValues());
        }
        return Map.of();
    }

    private <T> Function<HttpResponse, T> mapResponse(Class<T> responseClass) {
        return httpResponse -> {
            try {
                return objectMapper.readValue(httpResponse.getBody(), responseClass);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public Flow.Publisher<RequestStatus> payment(PaymentRequest request, String correlationId) {
        try {
            return new MapOperator<>(baseClient.post(PAYMENTS_URL, new SinglePublisher<>(objectMapper.writeValueAsString(request), executor), correlationId, findSimulationHeader(request)), httpResponse -> httpResponse.getStatus().toResponse());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Flow.Publisher<RequestStatus> rollbackPayment(String correlationId, String messageId) {
        return new MapOperator<>(baseClient.delete(String.format(ROLLBACK_PAYMENT_URL, messageId), correlationId), httpResponse -> httpResponse.getStatus().toResponse());
    }

    public Flow.Publisher<CaptureResponse> capture(String paymentId, CaptureRequest request, String correlationId) {
        try {
            return new MapOperator<>(baseClient.post(String.format(CAPTURE_URL, paymentId), new SinglePublisher<>(objectMapper.writeValueAsString(request), executor), correlationId), mapResponse(CaptureResponse.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Flow.Publisher<RequestStatus> cancel(String paymentId, String correlationId) {
        return new MapOperator<>(baseClient.post(String.format(CANCEL_URL, paymentId), new SinglePublisher<>("", executor), correlationId), httpResponse -> httpResponse.getStatus().toResponse());
    }

    public Flow.Publisher<RequestStatus> rollbackCapture(String paymentId, String messageId, String correlationId) {
        return new MapOperator<>(baseClient.delete(String.format(ROLLBACK_CAPTURE_URL, paymentId, messageId), correlationId), httpResponse -> httpResponse.getStatus().toResponse());
    }

    public Flow.Publisher<RefundResponse> refund(String paymentId, RefundRequest request, String correlationId) {
        try {
            return new MapOperator<>(baseClient.post(String.format(REFUND_URL, paymentId), new SinglePublisher<>(objectMapper.writeValueAsString(request), executor), correlationId), mapResponse(RefundResponse.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Flow.Publisher<RequestStatus> cutOffSettlementBatch(String merchantId, String batchNumber, String correlationId) {
        return new MapOperator<>(baseClient.put(String.format(SETTLEMENT_CUTOFF_URL, merchantId, batchNumber), correlationId), httpResponse -> httpResponse.getStatus().toResponse());
    }

    public Flow.Publisher<RequestStatus> rollbackRefund(String paymentId, String messageId, String correlationId) {
        return new MapOperator<>(baseClient.delete(String.format(ROLLBACK_REFUND_URL, paymentId, messageId), correlationId), httpResponse -> httpResponse.getStatus().toResponse());
    }
}
