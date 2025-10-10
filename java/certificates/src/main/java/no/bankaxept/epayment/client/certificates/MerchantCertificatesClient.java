package no.bankaxept.epayment.client.certificates;

import java.net.URL;
import no.bankaxept.epayment.client.base.BaseClient;

public final class MerchantCertificatesClient extends AbstractCertificatesClient {

  public MerchantCertificatesClient(BaseClient baseClient) {
    super(baseClient, "/merchant");
  }

  public MerchantCertificatesClient(
      URL authorizationServerUrl,
      URL resourceServerUrl,
      String merchantClientId,
      String merchantClientSecret
  ) {
    super(authorizationServerUrl, "/merchant", resourceServerUrl, merchantClientId, merchantClientSecret);
  }

}
