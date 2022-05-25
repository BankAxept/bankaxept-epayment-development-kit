package no.bankaxept.epayment.client.base.accesstoken;

import no.bankaxept.epayment.client.base.SinglePublisher;
import no.bankaxept.epayment.client.base.http.HttpClient;
import no.bankaxept.epayment.client.base.http.HttpResponse;
import no.bankaxept.epayment.client.base.http.HttpStatusException;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class SuppliedAccessTokenPublisher implements AccessTokenPublisher {

    private final Supplier<String> tokenSupplier;

    public SuppliedAccessTokenPublisher(Supplier<String> tokenSupplier) {
        this.tokenSupplier = tokenSupplier;
    }

    @Override
    public void subscribe(Flow.Subscriber<? super String> subscriber) {
        subscriber.onNext(tokenSupplier.get());
    }
}
