package no.bankaxept.epayment.test.client.tokenrequestor;

import no.bankaxept.epayment.client.base.RequestStatus;
import no.bankaxept.epayment.client.tokenrequestor.TokenRequestorClient;
import no.bankaxept.epayment.client.tokenrequestor.bankaxept.EnrolCardRequest;
import org.junit.jupiter.api.Test;
import reactor.adapter.JdkFlowAdapter;
import reactor.test.StepVerifier;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class TokenRequestorClientIntegrationTest {

  private TokenRequestorClient t1Client() throws MalformedURLException {
    return new TokenRequestorClient(
        new URL("https://t1-api.techcloud0dev.net/bankaxept-epayment/access-token-api/v1/accesstoken"),
        new URL("https://t1-api.techcloud0dev.net/bankaxept-epayment/token-requestor-api"),
        System.getenv("APIM_KEY"),
        System.getenv("CLIENT_ID"),
        System.getenv("CLIENT_SECRET")
    );
  }

  private TokenRequestorClient testClient() throws MalformedURLException {
    return new TokenRequestorClient(
        new URL("https://epp.stoetest.cloud/access-token/v1/accesstoken"),
        new URL("https://epp.stoetest.cloud/token-requestor"),
        System.getenv("CLIENT_ID"),
        System.getenv("CLIENT_SECRET")
    );
  }

  @Test
  public void enrolCardRequest() throws MalformedURLException {
    enrolCardRequest(t1Client());
    enrolCardRequest(testClient());
  }

  private void enrolCardRequest(TokenRequestorClient client) {
    var correlationId = UUID.randomUUID().toString();
    System.out.println("Correlation id: " + correlationId);
    StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(client.enrolCard(
            new EnrolCardRequest(),
            correlationId
        )))
        .expectNext(RequestStatus.ClientError)
        .verifyComplete();
  }

}
