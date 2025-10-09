package no.bankaxept.epayment.client.certificates;

import no.bankaxept.epayment.client.base.BaseClient;
import java.net.URL;

public class MerchantCertificatesClient extends AbstractCertificatesClient {

  public MerchantCertificatesClient(BaseClient baseClient) {
    super(baseClient);
  }

  public MerchantCertificatesClient(URL authorizationServerUrl, URL resourceServerUrl, String merchantClientId, String merchantClientSecret) {
    super(authorizationServerUrl, resourceServerUrl, merchantClientId, merchantClientSecret);
  }

  @Override
  protected String getEndpoint() {
    return "/merchant";
  }
}
