package no.bankaxept.epayment.client.base.accesstoken;

import reactor.core.publisher.Mono;

public class SuppliedAccessTokenPublisher {

  private final AccessTokenPublisher tokenPublisher;

  public SuppliedAccessTokenPublisher(AccessTokenPublisher tokenPublisher) {
    this.tokenPublisher = tokenPublisher;
  }

  public Mono<String> getAccessToken() {
    return tokenPublisher.getAccessToken();
  }

  public void shutDown() {
    tokenPublisher.shutDown();
  }
}
