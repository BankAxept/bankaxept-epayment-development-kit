package no.bankaxept.epayment.client.test.tokenrequestor;

import static no.bankaxept.epayment.client.test.Verifier.verifyBadRequest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Flow;
import no.bankaxept.epayment.client.base.RequestStatus;
import no.bankaxept.epayment.client.tokenrequestor.TokenRequestorClient;
import no.bankaxept.epayment.client.tokenrequestor.bankaxept.EnrolCardRequest;
import org.junit.jupiter.api.Test;

public class TokenRequestorClientIT {

  private TokenRequestorClient testClient() throws MalformedURLException {
    return new TokenRequestorClient(
        new URL(System.getenv("AUTHORIZATION_SERVER_URL")),
        new URL(System.getenv("TOKEN_REQUESTOR_API_URL")),
        System.getenv("CLIENT_ID"),
        System.getenv("CLIENT_SECRET")
    );
  }

  @Test
  public void enrolCardRequest() throws MalformedURLException {
    verifyBadRequest(enrolCardRequest(testClient()));
  }

  @Test
  public void eligibleBanksRequest() throws MalformedURLException {
    verifyBadRequest(testClient().eligibleBanks(List.of("090909")), "Invalid bank identifier 090909");
  }

  private Flow.Publisher<RequestStatus> enrolCardRequest(TokenRequestorClient client) {
    var correlationId = UUID.randomUUID().toString();
    System.out.println("Correlation id: " + correlationId);
    return client.enrolCard(new EnrolCardRequest(), correlationId);
  }

}
