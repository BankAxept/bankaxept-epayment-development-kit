package no.bankaxept.epayment.client.test.tokenrequestor;

import static no.bankaxept.epayment.client.test.Verifier.verifyBadRequest;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Flow;
import no.bankaxept.epayment.client.base.RequestStatus;
import no.bankaxept.epayment.client.tokenrequestor.TokenRequestorClient;
import no.bankaxept.epayment.client.tokenrequestor.bankaxept.EnrolCardRequest;
import org.junit.jupiter.api.Test;

public class TokenRequestorClientIT {

  private final TokenRequestorClient testClient;

  {
    try {
      testClient = new TokenRequestorClient(
          new URI("https://api.epp.stoetest.cloud/access-token/v1/accesstoken").toURL(),
          new URI("https://api.epp.stoetest.cloud/token-requestor").toURL(),
          System.getenv("CLIENT_ID"),
          System.getenv("CLIENT_SECRET")
      );
    } catch (MalformedURLException | URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void enrolCardRequest() {
    verifyBadRequest(enrolCardRequest(testClient));
  }

  @Test
  public void eligibleBanksRequest() {
    verifyBadRequest(testClient.eligibleBanks(List.of("090909")), "Invalid bank identifier 090909");
  }

  private Flow.Publisher<RequestStatus> enrolCardRequest(TokenRequestorClient client) {
    var correlationId = UUID.randomUUID().toString();
    System.out.println("Correlation id: " + correlationId);
    return client.enrolCard(new EnrolCardRequest(), correlationId);
  }

}
