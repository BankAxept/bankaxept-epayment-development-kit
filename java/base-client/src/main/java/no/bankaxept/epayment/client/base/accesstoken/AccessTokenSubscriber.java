package no.bankaxept.epayment.client.base.accesstoken;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import no.bankaxept.epayment.client.base.AccessFailed;
import no.bankaxept.epayment.client.base.accesstoken.publisher.AccessTokenPublisher;

public class AccessTokenSubscriber implements Flow.Subscriber<String> {

  private final CompletableFuture<String> token = new CompletableFuture<>();

  public AccessTokenSubscriber(AccessTokenPublisher tokenProcessor) {
    tokenProcessor.subscribe(this);
  }

  @Override
  public void onSubscribe(Flow.Subscription subscription) {
  }

  @Override
  public void onNext(String item) {
    token.complete(item);
  }

  @Override
  public void onError(Throwable throwable) {
    token.completeExceptionally(throwable);
  }

  @Override
  public void onComplete() {
  }

  public String get(Duration timeout) {
    try {
      return token.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
    } catch (ExecutionException e) {
      throw new AccessFailed(e.getCause());
    } catch (Exception e) {
      throw new AccessFailed(e);
    }
  }
}
