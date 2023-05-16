package no.bankaxept.epayment.test.client.base;

import no.bankaxept.epayment.client.base.SinglePublisher;
import no.bankaxept.epayment.client.base.accesstoken.publisher.AccessTokenPublisher;
import no.bankaxept.epayment.client.base.accesstoken.publisher.OnDemandAccessTokenPublisher;
import no.bankaxept.epayment.client.base.http.HttpClient;
import no.bankaxept.epayment.client.base.http.HttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;
import java.util.concurrent.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OnDemandAccessTokenPublisherTest {

  @Mock
  private HttpClient httpClientMock;

  @Mock
  private Flow.Subscriber<String> subscriberMock;

  @Mock
  private Flow.Subscriber<String> anotherSubscriberMock;

  private AccessTokenPublisher accessTokenPublisher;
  private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  @AfterEach
  public void tearDown() {
    accessTokenPublisher.shutDown();
  }

  @Test
  public void should_fetch_token_on_demand() throws IOException {
    mockToken(200, tokenResponse());
    accessTokenPublisher = createPublisher();
    accessTokenPublisher.subscribe(subscriberMock);
    verify(subscriberMock, timeout(1000)).onNext("a-token");
    accessTokenPublisher.subscribe(anotherSubscriberMock);
    verify(anotherSubscriberMock).onNext("a-token");
  }

  @Test
  public void should_handle_error() {
    mockToken(500, "error");
    accessTokenPublisher = createPublisher();
    accessTokenPublisher.subscribe(subscriberMock);
    verify(subscriberMock).onError(any());
  }

  private void mockToken(int status, String body) {
    doReturn(new SinglePublisher<>(new HttpResponse(status, body), executor))
        .when(httpClientMock).post(eq("uri"), any(), any());
  }

  private OnDemandAccessTokenPublisher createPublisher() {
    return new OnDemandAccessTokenPublisher.Builder()
        .httpClient(httpClientMock)
        .uri("uri")
        .clientCredentials("username", "password")
        .clock(clock)
        .apimKey("key")
        .build();
  }

  private String tokenResponse() throws IOException {
    return readJsonFromFile("token-response.json");
  }

  private String readJsonFromFile(@SuppressWarnings("SameParameterValue") String filename) throws IOException {
    return new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(getClass().getClassLoader()
        .getResource(filename)).getPath())));
  }
}
