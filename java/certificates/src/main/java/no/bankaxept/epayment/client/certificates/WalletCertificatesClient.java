package no.bankaxept.epayment.client.certificates;

import no.bankaxept.epayment.client.base.BaseClient;
import java.net.URL;

public class WalletCertificatesClient extends AbstractCertificatesClient {

  public WalletCertificatesClient(BaseClient baseClient) {
    super(baseClient);
  }

  public WalletCertificatesClient(URL authorizationServerUrl, URL resourceServerUrl, String walletClientId, String walletClientSecret) {
    super(authorizationServerUrl, resourceServerUrl, walletClientId, walletClientSecret);
  }

  @Override
  protected String getEndpoint() {
    return "/wallet";
  }
}
