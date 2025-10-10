package no.bankaxept.epayment.client.certificates;

import no.bankaxept.epayment.client.base.BaseClient;
import java.net.URL;

public final class MerchantCertificatesClient extends AbstractCertificatesClient {

  public MerchantCertificatesClient(BaseClient baseClient, String endpoint) {
    super(baseClient, endpoint);
  }

  public MerchantCertificatesClient(URL authorizationServerUrl, String endpoint, URL resourceServerUrl, String merchantClientId, String merchantClientSecret) {
    super(authorizationServerUrl, endpoint, resourceServerUrl, merchantClientId, merchantClientSecret);
  }

}
