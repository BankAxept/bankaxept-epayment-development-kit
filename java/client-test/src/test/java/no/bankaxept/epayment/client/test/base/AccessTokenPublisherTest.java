package no.bankaxept.epayment.client.test.base;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import no.bankaxept.epayment.client.base.SinglePublisher;
import no.bankaxept.epayment.client.base.accesstoken.AccessTokenPublisher;
import no.bankaxept.epayment.client.base.accesstoken.ScheduledAccessTokenPublisher;
import no.bankaxept.epayment.client.base.accesstoken.StaticAccessTokenPublisher;
import no.bankaxept.epayment.client.base.http.HttpClient;
import no.bankaxept.epayment.client.base.http.HttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessTokenPublisherTest {

  @Spy
  private ScheduledExecutorService schedulerMock = Executors.newScheduledThreadPool(1);

  @Mock
  private HttpClient httpClientMock;

  @Mock
  private Flow.Subscriber<String> subscriberMock;

  @Mock
  private Flow.Subscriber<String> anotherSubscriberMock;

  private AccessTokenPublisher accessTokenProcessor;
  private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  @AfterEach
  public void tearDown() {
    accessTokenProcessor.shutDown();
  }

  @Test
  public void should_schedule_on_startup_then_new_on_success() throws IOException {
    doReturn(new SinglePublisher<>(new HttpResponse(200, tokenResponse()), executor)).when(httpClientMock)
        .post(any(), any(), any());
    accessTokenProcessor = createPublisher();
    accessTokenProcessor.subscribe(subscriberMock);
    verify(schedulerMock).schedule(any(Runnable.class), eq(0L), eq(TimeUnit.SECONDS));
    verify(schedulerMock, Mockito.after(2000)).schedule(any(Runnable.class), eq(3590L), eq(TimeUnit.SECONDS));
    verify(subscriberMock).onNext("a-token");
    //To make sure we test the logic for "cached" tokens, we subscribe again after already getting a token
    accessTokenProcessor.subscribe(anotherSubscriberMock);
    verify(anotherSubscriberMock).onNext("a-token");
  }

  @Test
  public void should_schedule_on_startup_then_again_on_error() throws MalformedURLException {
    doReturn(new SinglePublisher<>(new HttpResponse(500, "error"), executor)).when(httpClientMock)
        .post(any(), any(), any());
    accessTokenProcessor = createPublisher();
    accessTokenProcessor.subscribe(subscriberMock);
    verify(schedulerMock).schedule(any(Runnable.class), eq(0L), eq(TimeUnit.SECONDS));
    verify(schedulerMock, Mockito.after(2000)).schedule(any(Runnable.class), eq(5L), eq(TimeUnit.SECONDS));
    verify(subscriberMock).onError(any());
  }


  @Test
  public void should_provide_static_token() {
    accessTokenProcessor = new StaticAccessTokenPublisher("static-token");
    accessTokenProcessor.subscribe(subscriberMock);
    verify(subscriberMock).onNext("static-token");
  }

  private ScheduledAccessTokenPublisher createPublisher() throws MalformedURLException {
    return new ScheduledAccessTokenPublisher.Builder()
        .httpClient(httpClientMock)
        .url(new URL("http://example.com"))
        .clientCredentials("username", "password")
        .clock(clock)
        .scheduler(schedulerMock)
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
