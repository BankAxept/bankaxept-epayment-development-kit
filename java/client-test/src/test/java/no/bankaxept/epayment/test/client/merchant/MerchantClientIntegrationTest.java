package no.bankaxept.epayment.test.client.merchant;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import no.bankaxept.epayment.client.base.RequestStatus;
import no.bankaxept.epayment.client.merchant.MerchantClient;
import no.bankaxept.epayment.client.merchant.PaymentRequest;
import org.junit.jupiter.api.Test;
import reactor.adapter.JdkFlowAdapter;
import reactor.test.StepVerifier;

public class MerchantClientIntegrationTest {

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
    MerchantClient client = t1Client();
    var correlationId = UUID.randomUUID().toString();
    StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(client.requestPayment(
            new PaymentRequest(),
            correlationId
        )))
        .expectNext(RequestStatus.ClientError)
        .verifyComplete();
  }

}
