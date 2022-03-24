package no.bankaxept.epayment.client.base;

import java.util.Queue;
import java.util.concurrent.Flow;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

public class EmptyResponseProcessor implements Flow.Processor<Object, Void> {

    private final AtomicReference<Object> atomicItem = new AtomicReference<>();
    private final AtomicReference<Throwable> atomicError = new AtomicReference<>();

    private final Queue<Flow.Subscriber<? super Void>> subscribers = new LinkedBlockingQueue<>();

    @Override
    public void subscribe(Flow.Subscriber<? super Void> subscriber) {
        Throwable error = atomicError.get();
        if (atomicItem.get() != null) {
            subscriber.onComplete();
        } else if (error != null) {
            subscriber.onError(error);
        } else {
            synchronized (subscribers) {
                subscribers.add(subscriber);
                subscriber.onSubscribe(new Flow.Subscription() {
                    @Override
                    public void request(long n) {
                        if(atomicItem.get() != null){
                            synchronized (subscribers) {
                                subscriber.onComplete();
                                subscribers.remove(subscriber);
                            }
                        }
                    }

                    @Override
                    public void cancel() {
                        synchronized (subscribers) {
                            subscriber.onComplete();
                            subscribers.remove(subscriber);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(1);
    }

    @Override
    public void onNext(Object item) {
        atomicItem.set(item);
        synchronized (subscribers) {
            subscribers.forEach(Flow.Subscriber::onComplete);
            subscribers.clear();
        }
    }

    @Override
    public void onError(Throwable throwable) {
        atomicError.set(throwable);
        synchronized (subscribers) {
            subscribers.forEach(subscriber -> subscriber.onError(throwable));
            subscribers.clear();
        }
    }

    @Override
    public void onComplete() {

    }
}
