package no.bankaxept.epayment.client.base.accesstoken;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;

class SinglePublisher<T> implements Flow.Publisher<T> {

    private final Executor executor;
    private final T item;

    public SinglePublisher(T item) {
        this.item = item;
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void subscribe(Flow.Subscriber<? super T> subscriber) {
        subscriber.onSubscribe(new Flow.Subscription() { // 1
            @Override
            public void request(long n) {
                if (n > 0)
                    executor.execute( // 4
                            () -> {
                                subscriber.onNext(item); // 2
                                subscriber.onComplete(); // 3
                            }
                    );
            }

            @Override
            public void cancel() {
            }
        });
    }
}