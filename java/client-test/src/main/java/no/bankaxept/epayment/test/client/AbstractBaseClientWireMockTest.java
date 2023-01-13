package no.bankaxept.epayment.test.client;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import no.bankaxept.epayment.client.base.BaseClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractBaseClientWireMockTest extends AbstractWireMockTest {

    protected BaseClient baseClient; //Because it fetches token on start, it needs to be started after setting up wiremock

    @BeforeEach
    public void setup(WireMockRuntimeInfo wmRuntimeInfo) {
        baseClient = createBaseClient(wmRuntimeInfo.getHttpPort());
    }

    @AfterEach
    public void tearDown() {
        baseClient.shutDown();
    }


    protected BaseClient createBaseClient(int port) {
        return new BaseClient.Builder("http://localhost:" + port).withStaticToken(AbstractWireMockTest.aToken).build();
    }

    protected BaseClient createScheduledBaseClient(int port) {
        return new BaseClient.Builder("http://localhost:" + port).apimKey("key").withScheduledToken("username", "password", clock).build();
    }

}
