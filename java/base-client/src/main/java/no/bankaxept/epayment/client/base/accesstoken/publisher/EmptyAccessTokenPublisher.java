package no.bankaxept.epayment.client.base.accesstoken.publisher;


import java.util.concurrent.Flow;

public class EmptyAccessTokenPublisher implements AccessTokenPublisher {

  @Override
  public void subscribe(Flow.Subscriber<? super String> subscriber) {
    throw new UnsupportedOperationException("EmptyAccessTokenPublisher can not provide tokens");
  }
}
