package no.bankaxept.epayment.client.base.accesstoken;

import no.bankaxept.epayment.client.base.http.HttpClient;
import no.bankaxept.epayment.client.base.http.HttpResponse;
import reactor.core.publisher.Mono;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import static java.nio.charset.StandardCharsets.UTF_8;

public class AccessTokenProvider {
  private final URL url;
  private final HttpClient httpClient;
  private final Map<String, List<String>> headers;
  private final String body;

  private AccessTokenProvider(URL url, HttpClient httpClient, Map<String, List<String>> headers,
      String body) {
    this.url = url;
    this.httpClient = httpClient;
    this.headers = headers;
    this.body = body;
  }

  public Mono<HttpResponse> fetchNewToken(){
    return httpClient.post(url.toString(), body, headers);
  }

  public URL getUrl() {
    return url;
  }

  public static class Builder {

    private URL url;
    private HttpClient httpClient;
    private ScheduledExecutorService scheduler;
    private final Map<String, List<String>> headers = new HashMap<>(Map.of(
        "Content-type",
        List.of("application/x-www-form-urlencoded")
    ));
    private GrantType grantType;
    private List<String> scopes = new ArrayList<>();

    public Builder httpClient(HttpClient httpClient) {
      this.httpClient = httpClient;
      return this;
    }

    public Builder url(URL url) {
      this.url = url;
      return this;
    }

    public Builder clientCredentials(String id, String secret) {
      headers.put(
          "Authorization",
          List.of("Basic " + Base64.getEncoder().encodeToString(
              (URLEncoder.encode(id, UTF_8) + ":" + URLEncoder.encode(secret, UTF_8)).getBytes(UTF_8)
          ))
      );
      return grantType(GrantType.client_credentials);
    }

    private Builder grantType(GrantType grantType) {
      this.grantType = grantType;
      return this;
    }

    public Builder scopes(List<String> scopes) {
      this.scopes.addAll(scopes);
      return this;
    }

    public AccessTokenProvider build() {
      if (grantType == null) {
        throw new IllegalArgumentException("Grant type is not set");
      }
      return new AccessTokenProvider(url, httpClient, headers, createBody());
    }

    private String createBody() {
      var body = new StringBuilder("grant_type=").append(grantType);
      if (!scopes.isEmpty()) {
        body.append("&").append("scope=").append(URLEncoder.encode(String.join(" ", scopes), UTF_8));
      }
      return body.toString();
    }
  }
}
