package no.bankaxept.epayment.client.test.base;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;
import no.bankaxept.epayment.client.base.accesstoken.AccessTokenProvider;
import no.bankaxept.epayment.client.base.accesstoken.AccessTokenPublisher;
import no.bankaxept.epayment.client.base.accesstoken.ScheduledAccessTokenPublisher;
import no.bankaxept.epayment.client.base.accesstoken.StaticAccessTokenPublisher;
import no.bankaxept.epayment.client.base.http.HttpClient;
import no.bankaxept.epayment.client.base.http.HttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class AccessTokenPublisherTest {

  @Mock
  private HttpClient httpClientMock;

  private ScheduledAccessTokenPublisher scheduledAccessTokenPublisherSpy;

  private AccessTokenPublisher staticAccessTokenPublisher;

  private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  @AfterEach
  public void tearDown() {
    if(staticAccessTokenPublisher != null) {
      staticAccessTokenPublisher.shutDown();
    }
    if(scheduledAccessTokenPublisherSpy != null) {
      scheduledAccessTokenPublisherSpy.shutDown();
    }
  }

  @Test
  public void should_schedule_on_startup_then_new_on_success() throws Exception {
    doReturn(Mono.just(new HttpResponse(200, tokenResponse()))).when(httpClientMock)
        .post(any(), any(), any());
    setupScheduledPublisher();
    StepVerifier.create(scheduledAccessTokenPublisherSpy.getAccessToken())
        .assertNext(token -> {
          assert token.equals("a-token");
          verify(httpClientMock).post(any(), any(), any());
          assert scheduledAccessTokenPublisherSpy.getLastTokenRefreshDelay() == 3590L;
        } )
        .verifyComplete();
    //To make sure we test the logic for "cached" tokens, we subscribe again after already getting a token
    StepVerifier.create(scheduledAccessTokenPublisherSpy.getAccessToken())
        .expectNext("a-token")
        .verifyComplete();
  }

  @Test
  public void should_schedule_on_startup_then_again_on_error() {
    doReturn(Mono.just(new HttpResponse(500, "Server Error"))).when(httpClientMock)
        .post(any(), any(), any());
    try{
      setupScheduledPublisher();
    }catch (Exception e) {
      StepVerifier.create(scheduledAccessTokenPublisherSpy.getAccessToken())
          .assertNext(token -> {
            verify(httpClientMock).post(any(), any(), any());
            assert scheduledAccessTokenPublisherSpy.getLastTokenRefreshDelay() == 5L;
          })
          .verifyComplete();
    }
  }


  @Test
  public void should_provide_static_token() {
    staticAccessTokenPublisher = new StaticAccessTokenPublisher("static-token");
    StepVerifier.create(staticAccessTokenPublisher.getAccessToken())
        .expectNext("static-token")
        .verifyComplete();
  }

  private void setupScheduledPublisher() throws Exception {
    AccessTokenProvider accessTokenProvider = new AccessTokenProvider.Builder()
        .httpClient(httpClientMock)
        .url(new URL("http://example.com"))
        .clientCredentials("username", "password")
        .build();

    scheduledAccessTokenPublisherSpy = new ScheduledAccessTokenPublisher(accessTokenProvider, clock);
  }

  private String tokenResponse() throws IOException {
    return readJsonFromFile("token-response.json");
  }

  private String readJsonFromFile(@SuppressWarnings("SameParameterValue") String filename) throws IOException {
    return new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(getClass().getClassLoader()
        .getResource(filename)).getPath())));
  }
}
