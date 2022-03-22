package no.bankaxept.epayment.client.merchant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.bankaxept.epayment.client.base.BaseClient;
import no.bankaxept.epayment.client.base.SinglePublisher;
import no.bankaxept.epayment.client.base.http.HttpResponse;
import no.bankaxept.epayment.swagger.merchant.api.PaymentRequest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

public class MerchantClient {

    private final BaseClient baseClient;

    private final static String PAYMENTS_URL = "/payments";

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private Executor executor = Executors.newSingleThreadExecutor();


    public MerchantClient(BaseClient baseClient) {
        this.baseClient = baseClient;
    }

    public MerchantClient(String baseurl, String apimKey, String username, String password) {
        this.baseClient = new BaseClient(baseurl, apimKey, username, password);
    }

    public Flow.Publisher<HttpResponse> payment(PaymentRequest request, String correlationId) throws JsonProcessingException {
        return baseClient.post(PAYMENTS_URL, new SinglePublisher<>(objectMapper.writeValueAsString(request), executor), correlationId);
    }

}
