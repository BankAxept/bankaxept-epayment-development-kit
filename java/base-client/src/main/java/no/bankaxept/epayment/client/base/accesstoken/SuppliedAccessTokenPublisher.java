package no.bankaxept.epayment.client.base.accesstoken;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

public class SuppliedAccessTokenPublisher implements AccessTokenPublisher {

  private final Sinks.One<String> tokenSink = Sinks.one();
  private final Sinks.One<Boolean> cancelSignal = Sinks.one();

  public SuppliedAccessTokenPublisher(Mono<String> tokenSupplier) {
    tokenSupplier.subscribe(tokenSink::tryEmitValue);
  }

  @Override
  public Mono<String> getAccessToken() {
    return tokenSink.asMono().takeUntilOther(cancelSignal.asMono());
  }

  @Override
  public void shutDown() {
    AccessTokenPublisher.super.shutDown();
    cancelSignal.tryEmitValue(true);
  }
}
