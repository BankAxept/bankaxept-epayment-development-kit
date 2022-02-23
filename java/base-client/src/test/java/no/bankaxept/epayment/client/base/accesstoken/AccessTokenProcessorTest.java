package no.bankaxept.epayment.client.base.accesstoken;

import no.bankaxept.epayment.client.base.http.HttpClient;
import no.bankaxept.epayment.client.base.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AccessTokenProcessorTest {

    @Spy
    public ScheduledExecutorService schedulerMock = Executors.newScheduledThreadPool(1);

    @Mock
    public HttpClient httpClientMock;

    @Mock
    public Flow.Subscriber<String> subscriberMock;

    public AccessTokenProcessor accessTokenProcessor;
    public final Clock clock  = Clock.fixed(Instant.now(), ZoneId.systemDefault());
    public final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Test
    public void should_schedule_on_startup_then_new_on_success() throws IOException {
        doReturn(new SinglePublisher<>(new HttpResponse(200, tokenResponseExpiresIn20Minutes()), executor)).when(httpClientMock).post(eq("uri"), any(), any());
        accessTokenProcessor = new AccessTokenProcessor("uri", "key", "username", "password", clock, schedulerMock, httpClientMock);
        accessTokenProcessor.subscribe(subscriberMock);
        verify(schedulerMock).schedule(any(Runnable.class), eq(0L), eq(TimeUnit.MILLISECONDS));
        verify(schedulerMock, Mockito.after(2000)).schedule(any(Runnable.class), eq(599999L), eq(TimeUnit.MILLISECONDS));
        verify(subscriberMock).onNext("a-token");
    }

    @Test
    public void should_schedule_on_startup_then_again_on_error() {
        doReturn(new SinglePublisher<>(new HttpResponse(500, "error"), executor)).when(httpClientMock).post(eq("uri"), any(), any());
        accessTokenProcessor = new AccessTokenProcessor("uri", "key", "username", "password", clock, schedulerMock, httpClientMock);
        accessTokenProcessor.subscribe(subscriberMock);
        verify(schedulerMock).schedule(any(Runnable.class), eq(0L), eq(TimeUnit.MILLISECONDS));
        verify(schedulerMock, Mockito.after(2000)).schedule(any(Runnable.class), eq(5000L), eq(TimeUnit.MILLISECONDS));
        verify(subscriberMock).onError(any());
    }

    private String tokenResponseExpiresIn20Minutes() throws IOException {
        return readJsonFromFile("token-response2.json").replace("123", Long.toString(clock.instant().plus(20, ChronoUnit.MINUTES).toEpochMilli()));
    }

    private String readJsonFromFile(@SuppressWarnings("SameParameterValue") String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource(filename)).getPath())));
    }
}