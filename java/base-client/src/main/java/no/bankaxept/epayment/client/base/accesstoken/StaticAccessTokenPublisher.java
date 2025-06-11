package no.bankaxept.epayment.client.base.accesstoken;

import reactor.core.publisher.Mono;

public class StaticAccessTokenPublisher implements AccessTokenPublisher {

  private String token;

  public StaticAccessTokenPublisher(String token) {
    this.token = token;
  }


  @Override
  public Mono<String> getAccessToken() {
    return Mono.just(token);
  }
}
