package no.bankaxept.epayment.client.certificates;

import no.bankaxept.epayment.client.base.BaseClient;
import java.net.URL;

public final class WalletCertificatesClient extends AbstractCertificatesClient {

  public WalletCertificatesClient(BaseClient baseClient, String endpoint) {
    super(baseClient, endpoint);
  }

  public WalletCertificatesClient(URL authorizationServerUrl, String endpoint, URL resourceServerUrl, String walletClientId, String walletClientSecret) {
    super(authorizationServerUrl, endpoint, resourceServerUrl, walletClientId, walletClientSecret);
  }

}
