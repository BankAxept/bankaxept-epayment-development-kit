package no.bankaxept.epayment.client.base.accesstoken;

import no.bankaxept.epayment.client.base.http.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Flow;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AccessTokenProcessorTest {
    @Mock
    private ScheduledExecutorService schedulerMock;

    @Mock
    private HttpClient httpClientMock;

    private AccessTokenProcessor accessTokenProcessor;
    private Clock clock  = Clock.systemDefaultZone();


    @Test
    public void should_schedule_on_startup() throws IOException {
        doReturn( new SimplePublisher(tokenResponseIn20Minutes())).when(httpClientMock).post(eq("uri"), any(), any());
        accessTokenProcessor = new AccessTokenProcessor("uri", "key", "username", "password", clock, schedulerMock, httpClientMock);
        verify(schedulerMock).schedule(any(Runnable.class), eq(0L), eq(TimeUnit.MILLISECONDS));
        verify(schedulerMock, Mockito.after(2000)).schedule(any(Runnable.class), eq(599999L), eq(TimeUnit.MILLISECONDS));
    }

    private String tokenResponseIn20Minutes() throws IOException {
        return readJsonFromFile("token-response2.json").replace("123", Long.toString(clock.instant().plus(20, ChronoUnit.MINUTES).toEpochMilli()));
    }

    private String readJsonFromFile(String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(filename).getPath())));
    }


    private class SimplePublisher implements Flow.Publisher<String> {
        private String message;

        public SimplePublisher(String message) {
            this.message = message;
        }

        @Override
        public void subscribe(Flow.Subscriber<? super String> subscriber) {
            subscriber.onNext(message);
            subscriber.onComplete();
        }
    }

}