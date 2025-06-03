package no.bankaxept.epayment.client.base.accesstoken;

import reactor.core.publisher.Mono;

public interface AccessTokenPublisher {

  Mono<String> getAccessToken();
  default void shutDown() {}
}
