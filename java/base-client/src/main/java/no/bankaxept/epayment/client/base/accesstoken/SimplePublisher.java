package no.bankaxept.epayment.client.base.accesstoken;

import java.util.concurrent.Flow;

class SimplePublisher<T> implements Flow.Publisher<T> {
    private T item;

    public SimplePublisher(T item) {
        this.item = item;
    }

    @Override
    public void subscribe(Flow.Subscriber<? super T> subscriber) {
        subscriber.onNext(item);
        subscriber.onComplete();
    }
}