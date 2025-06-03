package no.bankaxept.epayment.client.test.base;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Clock;
import java.util.ServiceLoader;
import no.bankaxept.epayment.client.base.accesstoken.AccessTokenProvider;
import no.bankaxept.epayment.client.base.accesstoken.ScheduledAccessTokenPublisher;
import no.bankaxept.epayment.client.base.http.HttpClient;
import no.bankaxept.epayment.client.base.spi.HttpClientProvider;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

public class BaseClientIT {

  @Test
  public void accessToken() throws MalformedURLException {
    HttpClient httpClient = ServiceLoader.load(HttpClientProvider.class)
        .findFirst()
        .map(httpClientProvider -> httpClientProvider.create(null))
        .orElseThrow();
    var accessTokenProvider = new AccessTokenProvider.Builder()
        .httpClient(httpClient)
        .url(new URL(System.getenv("AUTHORIZATION_SERVER_URL")))
        .clientCredentials(System.getenv("CLIENT_ID_COMPLEX_SECRET"), System.getenv("CLIENT_SECRET_COMPLEX_SECRET"))
        .build();
    var publisher = new ScheduledAccessTokenPublisher(accessTokenProvider, Clock.systemDefaultZone());
    StepVerifier.create(publisher.getAccessToken())
        .expectNextMatches(s -> s.startsWith("eyJ"))
        .verifyComplete();
  }

}
