package no.bankaxept.epayment.client.base.accesstoken;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import no.bankaxept.epayment.client.base.http.HttpClient;
import no.bankaxept.epayment.client.base.http.HttpResponse;

public class ScheduledAccessTokenPublisher extends HttpAccessTokenPublisher {

  private final ScheduledExecutorService scheduler;

  private boolean shutDown;

  ScheduledAccessTokenPublisher(
      String uri,
      Map<String, List<String>> headers,
      String body,
      Clock clock,
      ScheduledExecutorService scheduler,
      HttpClient httpClient
  ) {
    super(uri, httpClient, headers, body, clock);
    this.scheduler = scheduler;
    scheduleFetch(0);
  }

  @Override
  public void onNext(HttpResponse item) {
    super.onNext(item);
    var token = atomicToken.get();
    if (token != null) {
      scheduleFetch(token.secondsUntilTenSecondsBeforeExpiry(clock));
    }
  }

  @Override
  public void onError(Throwable throwable) {
    scheduleFetch(5);
    super.onError(throwable);
  }

  private void scheduleFetch(long seconds) {
    if (shutDown)
      return;
    scheduler.schedule(this::fetchNewToken, seconds, TimeUnit.SECONDS);
  }

  public void shutDown() {
    shutDown = true;
    scheduler.shutdown();
    fetchExecutor.shutdown();
    try {
      while (!scheduler.awaitTermination(500, TimeUnit.MILLISECONDS) && !fetchExecutor.awaitTermination(
          500,
          TimeUnit.MILLISECONDS
      )) {
      }
    } catch (InterruptedException ignored) {
    }
  }

  public static class Builder extends HttpAccessTokenPublisher.Builder<ScheduledAccessTokenPublisher> {

    private ScheduledExecutorService scheduler;

    public Builder scheduler(ScheduledExecutorService scheduler) {
      this.scheduler = scheduler;
      return this;
    }

    public ScheduledAccessTokenPublisher build() {
      if (grantType == null) {
        throw new IllegalArgumentException("Grant type is not set");
      }
      return new ScheduledAccessTokenPublisher(uri, headers, createBody(), clock, getSchedulerOrDefault(), httpClient);
    }

    private ScheduledExecutorService getSchedulerOrDefault() {
      return scheduler == null ? Executors.newScheduledThreadPool(1) : scheduler;
    }

  }
}
