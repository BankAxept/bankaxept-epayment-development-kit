package no.bankaxept.epayment.client.base.accesstoken;

import no.bankaxept.epayment.client.base.SinglePublisher;
import no.bankaxept.epayment.client.base.http.HttpClient;
import no.bankaxept.epayment.client.base.http.HttpResponse;
import no.bankaxept.epayment.client.base.http.HttpStatusException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

abstract class HttpAccessTokenPublisher implements AccessTokenPublisher, Flow.Subscriber<HttpResponse> {

  protected final ExecutorService fetchExecutor = Executors.newSingleThreadExecutor();

  private final String uri;
  private final HttpClient httpClient;
  private final Map<String, List<String>> headers;
  private final String body;
  protected final Clock clock;

  protected final AtomicReference<AccessToken> atomicToken = new AtomicReference<>();

  protected final Queue<Subscriber<? super String>> subscribers = new LinkedBlockingQueue<>();

  public HttpAccessTokenPublisher(
      String uri, HttpClient httpClient, Map<String, List<String>> headers, String body, Clock clock
  ) {
    this.uri = uri;
    this.httpClient = httpClient;
    this.headers = headers;
    this.body = body;
    this.clock = clock;
  }

  protected void fetchNewToken() {
    httpClient.post(uri, new SinglePublisher<>(body, fetchExecutor), headers).subscribe(this);
  }

  @Override
  public void onNext(HttpResponse item) {
    if (!item.getStatus().is2xxOk()) {
      onError(new HttpStatusException(item.getStatus(), "Error when fetching token"));
      return;
    }
    AccessToken token;
    try {
      token = AccessToken.parse(item.getBody(), clock);
    } catch (Exception e) {
      onError(e);
      return;
    }
    atomicToken.set(token);
    synchronized (subscribers) {
      subscribers.forEach(subscriber -> subscriber.onNext(token.getToken()));
      subscribers.clear();
    }
  }

  @Override
  public void subscribe(Flow.Subscriber<? super String> subscriber) {
    var token = atomicToken.get();
    if (token != null && token.getExpiry().isAfter(clock.instant()))
      subscriber.onNext(token.getToken());
    else {
      synchronized (subscribers) {
        subscribers.add(subscriber);
      }
    }
  }

  @Override
  public void onError(Throwable throwable) {
    synchronized (subscribers) {
      subscribers.forEach(subscriber -> subscriber.onError(throwable));
      subscribers.clear();
    }
  }

  @Override
  public void onSubscribe(Flow.Subscription subscription) {
    subscription.request(1);
  }

  @Override
  public void onComplete() {
  }

  @Override
  public void shutDown() {
    fetchExecutor.shutdown();
  }


  public abstract static class Builder<T extends HttpAccessTokenPublisher> {

    protected String uri;
    protected HttpClient httpClient;
    protected Clock clock = Clock.systemDefaultZone();
    protected GrantType grantType;
    protected List<String> scopes = new ArrayList<>();

    protected final Map<String, List<String>> headers = new HashMap<>(Map.of("Content-type",
        List.of("application/x-www-form-urlencoded")
    ));


    public Builder<T> httpClient(HttpClient httpClient) {
      this.httpClient = httpClient;
      return this;
    }

    public Builder<T> uri(String uri) {
      this.uri = uri;
      return this;
    }

    public Builder<T> apimKey(String apimKey) {
      headers.put("Ocp-Apim-Subscription-Key", List.of(apimKey));
      return this;
    }

    public Builder<T> clientCredentials(String id, String secret) {
      headers.put("Authorization",
          List.of("Basic " + Base64.getEncoder().encodeToString((id + ":" + secret).getBytes(StandardCharsets.UTF_8)))
      );
      return grantType(GrantType.client_credentials);
    }

    private Builder<T> grantType(GrantType grantType) {
      this.grantType = grantType;
      return this;
    }

    public Builder<T> scopes(List<String> scopes) {
      this.scopes.addAll(scopes);
      return this;
    }

    public Builder<T> clock(Clock clock) {
      this.clock = clock;
      return this;
    }

    public abstract T build();

    protected String createBody() {
      var body = new StringBuilder("grant_type=").append(grantType);
      if (!scopes.isEmpty()) {
        body.append("&").append("scope=").append(URLEncoder.encode(String.join(" ", scopes), StandardCharsets.UTF_8));
      }
      return body.toString();
    }
  }

}
