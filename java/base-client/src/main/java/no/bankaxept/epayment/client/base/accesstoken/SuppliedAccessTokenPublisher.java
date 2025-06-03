package no.bankaxept.epayment.client.base.accesstoken;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import java.util.function.Supplier;

public class SuppliedAccessTokenPublisher implements AccessTokenPublisher {

  private final Sinks.One<String> tokenSink = Sinks.one();
  private final Sinks.One<Boolean> cancelSignal = Sinks.one();

  public SuppliedAccessTokenPublisher(Supplier<String> tokenSupplier) {
    Mono.fromSupplier(tokenSupplier).subscribe(tokenSink::tryEmitValue);
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
