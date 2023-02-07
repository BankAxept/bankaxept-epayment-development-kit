package no.bankaxept.epayment.client.base.accesstoken;


import java.util.concurrent.Flow;

public class StaticAccessTokenPublisher implements AccessTokenPublisher {

    private String token;

    public StaticAccessTokenPublisher(String token) {
        this.token = token;
    }

    @Override
    public void subscribe(Flow.Subscriber<? super String> subscriber) {
        subscriber.onNext(token);
    }
}
