package no.bankaxept.epayment.client.base.accesstoken;

import java.util.concurrent.Flow;

public interface AccessTokenPublisher extends Flow.Publisher<String> {

  default void shutDown() {}
}
