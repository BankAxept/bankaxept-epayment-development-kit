package no.bankaxept.epayment.client.test;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import no.bankaxept.epayment.client.base.BaseClient;
import no.bankaxept.epayment.client.base.SinglePublisher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

@BaseClientWireMockTest
public abstract class AbstractBaseClientWireMockTest {

  protected BaseClient baseClient; //Because it fetches token on start, it needs to be started after setting up wiremock
  private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
  private final static String aToken = "a-token";

  private final String validTokenResponseTemplate = "{\n" +
      "\"expires_in\": " + 7200 + ",\n" +
      "\"access_token\": \"%s\"\n" +
      "}";

  private final Executor executor = Executors.newSingleThreadExecutor();

  @BeforeEach
  public void setup(WireMockRuntimeInfo wmRuntimeInfo) {
    baseClient = createBaseClient(wmRuntimeInfo.getHttpPort());
  }

  @AfterEach
  public void tearDown() {
    baseClient.shutDown();
  }

  protected MappingBuilder tokenEndpointMapping(ResponseDefinitionBuilder responseBuilder) {
    return WireMock.post("/bankaxept-epayment/access-token-api/v1/accesstoken")
        .withHeader("Ocp-Apim-Subscription-Key", new EqualToPattern("key"))
        .withBasicAuth("username", "password")
        .willReturn(responseBuilder);
  }

  protected ResponseDefinitionBuilder validTokenResponse() {
    return validTokenResponse(aToken);
  }

  protected ResponseDefinitionBuilder validTokenResponse(String token) {
    return WireMock.ok().withBody(String.format(validTokenResponseTemplate, token));
  }

  protected BaseClient createBaseClient(int port) {
    return new BaseClient.Builder("http://localhost:" + port).withStaticToken(aToken).build();
  }

  protected BaseClient createScheduledBaseClient(int port) {
    return new BaseClient.Builder("http://localhost:" + port).apimKey("key")
        .withScheduledToken(
            "http://localhost:" + port + "/bankaxept-epayment/access-token-api/v1/accesstoken",
            "username",
            "password",
            clock
        )
        .build();
  }

  protected Flow.Publisher<String> emptyPublisher() {
    return new SinglePublisher<>("", executor);
  }

  protected String bearerToken() {
    return "Bearer " + aToken;
  }

}
