package no.bankaxept.epayment.client.test.wallet;

import static no.bankaxept.epayment.client.test.Verifier.verifyBadRequest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.Flow;
import no.bankaxept.epayment.client.base.RequestStatus;
import no.bankaxept.epayment.client.wallet.WalletClient;
import no.bankaxept.epayment.client.wallet.bankaxept.PaymentRequest;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public class WalletClientIT {

  private WalletClient testClient() throws MalformedURLException {
    return new WalletClient(
        new URL("https://api.epp-stoetest.cloud/access-token/v1/accesstoken"),
        new URL("https://api.epp-stoetest.cloud/wallet"),
        System.getenv("CLIENT_ID"),
        System.getenv("CLIENT_SECRET")
    );
  }

  @Test
  public void paymentRequest() throws MalformedURLException {
    verifyBadRequest(paymentRequest(testClient()), "");
  }

  private Mono<RequestStatus> paymentRequest(WalletClient client) {
    var correlationId = UUID.randomUUID().toString();
    System.out.println("Correlation id: " + correlationId);
    return client.requestPayment(new PaymentRequest(), correlationId);
  }

}
