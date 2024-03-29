package no.bankaxept.epayment.client.test.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import java.time.Duration;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeoutException;
import no.bankaxept.epayment.client.base.AccessFailed;
import no.bankaxept.epayment.client.base.accesstoken.AccessTokenSubscriber;
import no.bankaxept.epayment.client.base.accesstoken.ScheduledAccessTokenPublisher;
import no.bankaxept.epayment.client.base.http.HttpStatus;
import no.bankaxept.epayment.client.base.http.HttpStatusException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

@ExtendWith(MockitoExtension.class)
public class AccessTokenSubscriberTest {

  private AccessTokenSubscriber subscriber;

  @Mock
  private ScheduledAccessTokenPublisher processorMock;

  @Test
  public void should_provide_token_from_processor() {
    doAnswer((Answer<Void>) invocation -> {
      ((Flow.Subscriber<String>) invocation.getArgument(0)).onNext("a-token");
      return null;
    }).when(processorMock).subscribe(any());
    subscriber = new AccessTokenSubscriber(processorMock);
    assertThat(subscriber.get(Duration.ofSeconds(1))).isEqualTo("a-token");
  }

  @Test
  public void should_propagate_server_error() {
    HttpStatusException serverError = new HttpStatusException(new HttpStatus(500), "Server error");
    doAnswer((Answer<Void>) invocation -> {
      ((Flow.Subscriber<String>) invocation.getArgument(0)).onError(serverError);
      return null;
    }).when(processorMock).subscribe(any());
    subscriber = new AccessTokenSubscriber(processorMock);
    assertThatThrownBy(() -> subscriber.get(Duration.ofSeconds(1)))
        .isInstanceOf(AccessFailed.class)
        .hasCause(serverError);
  }

  @Test
  public void timeout() {
    subscriber = new AccessTokenSubscriber(processorMock);
    assertThatThrownBy(() -> subscriber.get(Duration.ofSeconds(1)))
        .isInstanceOf(AccessFailed.class)
        .hasCauseInstanceOf(TimeoutException.class);
  }

}
