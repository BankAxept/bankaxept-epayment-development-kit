package no.bankaxept.epayment.client.base.accesstoken;


import java.util.concurrent.*;
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
