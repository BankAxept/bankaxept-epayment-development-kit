package no.bankaxept.epayment.client.merchant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.bankaxept.epayment.client.base.BaseClient;
import no.bankaxept.epayment.client.base.AbstractClient;
import no.bankaxept.epayment.client.base.Response;
import no.bankaxept.epayment.client.base.SinglePublisher;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;

public class MerchantClient extends AbstractClient {

    private final static String PAYMENTS_URL = "/payments";

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final Executor executor = Executors.newSingleThreadExecutor();

    public MerchantClient(BaseClient baseClient) {
        super(baseClient);
    }

    public MerchantClient(String baseurl, String apimKey, String username, String password) {
        super(baseurl, apimKey, username, password);
    }

    public Flow.Publisher<Response> payment(PaymentRequest request, String correlationId) throws JsonProcessingException {
        return postEmptyResponseBody(PAYMENTS_URL, new SinglePublisher<>(objectMapper.writeValueAsString(request), executor), correlationId);
    }

}
