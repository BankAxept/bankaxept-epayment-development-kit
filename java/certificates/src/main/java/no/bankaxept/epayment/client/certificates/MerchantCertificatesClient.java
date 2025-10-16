package no.bankaxept.epayment.client.certificates;

import java.net.URL;
import no.bankaxept.epayment.client.base.BaseClient;

public final class MerchantCertificatesClient extends AbstractCertificatesClient {

  public MerchantCertificatesClient(BaseClient baseClient) {
    super(baseClient, "/v1/merchant");
  }

  public MerchantCertificatesClient(
      URL authorizationServerUrl,
      URL resourceServerUrl,
      String clientId,
      String clientSecret
  ) {
    this(new BaseClient.Builder(resourceServerUrl)
        .withScheduledToken(authorizationServerUrl, clientId, clientSecret)
        .build());
  }

}
