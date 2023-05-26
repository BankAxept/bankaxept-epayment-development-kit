package no.bankaxept.epayment.client.base.accesstoken;

import no.bankaxept.epayment.client.base.http.HttpClient;
import java.time.Clock;
import java.util.*;
import java.util.concurrent.*;

public class OnDemandAccessTokenPublisher extends HttpAccessTokenPublisher {

  private OnDemandAccessTokenPublisher(
      String uri,
      Map<String, List<String>> headers,
      String body,
      Clock clock,
      HttpClient httpClient
  ) {
    super(uri, httpClient, headers, body, clock);
  }


  @Override
  public void subscribe(Flow.Subscriber<? super String> subscriber) {
    super.subscribe(subscriber);
    synchronized (subscribers) {
      if (!subscribers.isEmpty()) {
        fetchNewToken();
      }
    }
  }

  public static class Builder extends HttpAccessTokenPublisher.Builder<OnDemandAccessTokenPublisher> {

    public OnDemandAccessTokenPublisher build() {
      return new OnDemandAccessTokenPublisher(uri, headers, createBody(), clock, httpClient);
    }
  }

}
