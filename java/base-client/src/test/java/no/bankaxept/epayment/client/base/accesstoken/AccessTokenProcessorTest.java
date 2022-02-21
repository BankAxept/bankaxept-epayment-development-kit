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
import java.util.concurrent.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AccessTokenProcessorTest {

    //Everything needs to be public because this package isn't exported

    @Spy
    public ScheduledExecutorService schedulerMock = Executors.newScheduledThreadPool(1);

    @Mock
    public HttpClient httpClientMock;
    public AccessTokenProcessor accessTokenProcessor;
    public Clock clock  = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    @Test
    public void should_schedule_on_startup() throws IOException {
        doReturn(new SimplePublisher<>(new HttpResponse(200, tokenResponseIn20Minutes()))).when(httpClientMock).post(eq("uri"), any(), any());
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
}