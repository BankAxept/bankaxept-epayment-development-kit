package no.bankaxept.epayment.client.base;

import no.bankaxept.epayment.client.base.exception.NonRetryableException;
import no.bankaxept.epayment.client.base.exception.RetryableException;
import no.bankaxept.epayment.client.base.exception.SdkException;
import no.bankaxept.epayment.client.base.http.HttpResponse;
import no.bankaxept.epayment.client.base.http.HttpStatusException;

import java.util.Queue;
import java.util.concurrent.Flow;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

public class EmptyResponseProcessor implements Flow.Processor<HttpResponse, Void> {

    private final AtomicReference<HttpResponse> atomicItem = new AtomicReference<>();
    private final AtomicReference<Throwable> atomicError = new AtomicReference<>();

    private final Queue<Flow.Subscriber<? super Void>> subscribers = new LinkedBlockingQueue<>();

    @Override
    public void subscribe(Flow.Subscriber<? super Void> subscriber) {
        synchronized (subscribers) {
            subscriber.onSubscribe(new Flow.Subscription() {
                @Override
                public void request(long n) {
                    if (n > 0) {
                        Throwable error = atomicError.get();
                        if (atomicItem.get() != null)
                            subscriber.onComplete();
                        else if (error != null)
                            subscriber.onError(error);
                        else
                            subscribers.add(subscriber);
                    }
                }

                @Override
                public void cancel() {
                    synchronized (subscribers) {
                        subscribers.remove(subscriber);
                    }
                }
            });
        }
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(1);
    }

    @Override
    public void onNext(HttpResponse item) {
        if (item.getStatus().is2xxOk()) {
            atomicItem.set(item);
            synchronized (subscribers) {
                subscribers.forEach(Flow.Subscriber::onComplete);
                subscribers.clear();
            }
            return;
        }
        if (item.getStatus().is4xxClientError()) {
            onError(new NonRetryableException());
        } else if (item.getStatus().is5xxServerError()) {
            onError(new RetryableException());
        } else {
            onError(new SdkException(new HttpStatusException(item.getStatus(), "Unexpected http status")));
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
