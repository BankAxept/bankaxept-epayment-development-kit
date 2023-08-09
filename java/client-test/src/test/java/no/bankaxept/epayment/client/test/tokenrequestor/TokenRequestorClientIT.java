package no.bankaxept.epayment.client.test.tokenrequestor;

import static no.bankaxept.epayment.client.test.Verifier.verifyBadRequest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import no.bankaxept.epayment.client.tokenrequestor.TokenRequestorClient;
import no.bankaxept.epayment.client.tokenrequestor.bankaxept.EnrolCardRequest;
import org.junit.jupiter.api.Test;

public class TokenRequestorClientIT {

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
    verifyBadRequest(client.enrolCard(new EnrolCardRequest(), correlationId));
  }

}
