package no.bankaxept.epayment.client.base.accesstoken;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import no.bankaxept.epayment.client.base.http.HttpStatusException;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

public class ScheduledAccessTokenPublisher implements AccessTokenPublisher {
  private final AccessTokenProvider accessTokenProvider;
  private final Clock clock;

  private final Sinks.Many<AccessToken> tokenSink;
  private final Sinks.Many<Boolean> triggerRetrySink = Sinks.many().replay().latest();
  private final Sinks.One<Boolean> cancelSignal = Sinks.one();

  private long lastTokenRefreshDelay = 0;

  public ScheduledAccessTokenPublisher(
      AccessTokenProvider accessTokenProvider,
      Clock clock
  ) {
    this.accessTokenProvider = accessTokenProvider;
    this.clock = clock;
    this.tokenSink = Sinks.many().replay().latest();
    this.triggerRetrySink.asFlux()
        .takeUntilOther(cancelSignal.asMono())
        .flatMap(signal -> retryAndSave())
        .subscribe();
    this.triggerRetrySink.tryEmitNext(true);//first attempt
  }

  public Mono<String> getAccessToken(){
    AtomicBoolean isInfiniteLoop = new AtomicBoolean(false);
    return tokenSink.asFlux()
        .onErrorResume(previousError -> {
          if(!isInfiniteLoop.get()){
            isInfiniteLoop.set(true); // Prevents infinite loop in case of repeated errors
            return retryAndSave();
          } else {
            return Mono.error(previousError);
          }
        })
        .map(AccessToken::getToken)
        .next();
  }

  public void shutDown() {
    cancelSignal.tryEmitValue(true);
  }

  private Mono<AccessToken> retryAndSave() {
    return retryFetch()
        .doOnNext(tokenSink::tryEmitNext)
        .doOnError(tokenSink::tryEmitError);
  }

  private Mono<AccessToken> retryFetch() {
    return Mono.fromCallable(()-> fetchToken())
        .flatMap(i->i)
        .doOnNext(accessToken -> refreshTokenIn(accessToken.secondsUntilTenSecondsBeforeExpiry(clock)) )
        .doOnError(e -> refreshTokenIn(5));
  }

  private Mono<AccessToken> fetchToken(){
    return accessTokenProvider.fetchNewToken()
        .map(response -> {
          if (!response.getStatus().is2xxOk()) {
            throw new HttpStatusException(response.getStatus(),
                String.format("Could not get access token from %s. HTTP status: %s. HTTP payload: %s",
                    accessTokenProvider.getUrl().toString(), response.getStatus(), response.getBody()));
          }
          AccessToken accessToken = AccessToken.parse(response.getBody(), clock);
          if(!accessToken.getExpiry().isAfter(clock.instant())){
            throw new IllegalStateException("Access token is not valid yet");
          }
          return accessToken;
        });
  }

  private void refreshTokenIn(long delay) {
    lastTokenRefreshDelay = delay;
    Mono.just(true)
        .delayElement(Duration.ofSeconds(delay))
        .subscribe(triggerRetrySink::tryEmitNext);
  }

  public long getLastTokenRefreshDelay() {
    return lastTokenRefreshDelay;
  }
}
