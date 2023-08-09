package no.bankaxept.epayment.client.test.merchant;

import static no.bankaxept.epayment.client.test.Verifier.verifyBadRequest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.Flow;
import no.bankaxept.epayment.client.base.RequestStatus;
import no.bankaxept.epayment.client.merchant.MerchantClient;
import no.bankaxept.epayment.client.merchant.PaymentRequest;
import org.junit.jupiter.api.Test;

public class MerchantClientIT {

  private MerchantClient t1Client() throws MalformedURLException {
    return new MerchantClient(
        new URL("https://t1-api.techcloud0dev.net/bankaxept-epayment/access-token-api/v1/accesstoken"),
        new URL("https://t1-api.techcloud0dev.net/bankaxept-epayment/merchant-api"),
        System.getenv("APIM_KEY"),
        System.getenv("CLIENT_ID"),
        System.getenv("CLIENT_SECRET")
    );
  }

  private MerchantClient testClient() throws MalformedURLException {
    return new MerchantClient(
        new URL("https://epp.stoetest.cloud/access-token/v1/accesstoken"),
        new URL("https://epp.stoetest.cloud/merchant"),
        System.getenv("CLIENT_ID"),
        System.getenv("CLIENT_SECRET")
    );
  }

  @Test
  public void paymentRequest() throws MalformedURLException {
    verifyBadRequest(paymentRequest(t1Client()));
    verifyBadRequest(paymentRequest(testClient()));
  }

  private Flow.Publisher<RequestStatus> paymentRequest(MerchantClient client) {
    var correlationId = UUID.randomUUID().toString();
    System.out.println("Correlation id: " + correlationId);
    return client.requestPayment(new PaymentRequest(), correlationId);
  }

}
