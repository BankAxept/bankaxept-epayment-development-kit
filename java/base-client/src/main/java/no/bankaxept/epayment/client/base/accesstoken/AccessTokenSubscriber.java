package no.bankaxept.epayment.client.base.accesstoken;

import java.time.Duration;
import java.util.concurrent.*;

public class AccessTokenSubscriber implements Flow.Subscriber<String> {

    private final CompletableFuture<String> token = new CompletableFuture<>();
    private Flow.Subscription subscription;

    public AccessTokenSubscriber(AccessTokenProcessor supplier) {
        supplier.subscribe(this);
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(String item) {
        token.complete(item);
    }

    @Override
    public void onError(Throwable throwable) {
        token.completeExceptionally(throwable);
    }

    @Override
    public void onComplete() {
    }

    public String get(Duration timeout) throws ExecutionException, InterruptedException, TimeoutException {
        try {
            return token.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } finally {
            if (subscription != null)
                subscription.cancel();
        }
    }
}
