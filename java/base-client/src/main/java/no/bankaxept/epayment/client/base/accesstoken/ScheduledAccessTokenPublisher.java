package no.bankaxept.epayment.client.base.accesstoken;

import java.time.Clock;
import java.time.Duration;
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
        .flatMap(b -> tryToUpdateToken())
        .doOnNext(accessToken -> refreshTokenIn(accessToken.secondsUntilTenSecondsBeforeExpiry(clock)) )
        .doOnError(e -> refreshTokenIn(5))
        .subscribe(
            tokenSink::tryEmitNext,
            tokenSink::tryEmitError
        );
    this.triggerRetrySink.tryEmitNext(true);//first attempt
  }

  public Mono<String> getAccessToken(){
    return tokenSink.asFlux()
        .map(AccessToken::getToken)
        .next();
  }

  public void shutDown() {
    cancelSignal.tryEmitValue(true);
  }

  private Mono<AccessToken> tryToUpdateToken(){
    return accessTokenProvider.fetchNewToken()
        .flatMap(response -> {
          if (!response.getStatus().is2xxOk()) {
            return Mono.error(new HttpStatusException(response.getStatus(),
                String.format("Could not get access token from %s. HTTP status: %s. HTTP payload: %s",
                    accessTokenProvider.getUrl().toString(), response.getStatus(), response.getBody())));
          }

          try {
            AccessToken token = AccessToken.parse(response.getBody(), clock);
            return Mono.just(token);
          } catch (Exception e) {
            return Mono.error(e);
          }
        })
        .map(accessToken -> {
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
