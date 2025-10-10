package no.bankaxept.epayment.client.certificates;

import java.net.URL;
import no.bankaxept.epayment.client.base.BaseClient;

public final class WalletCertificatesClient extends AbstractCertificatesClient {

  public WalletCertificatesClient(BaseClient baseClient) {
    super(baseClient, "/wallet");
  }

  public WalletCertificatesClient(
      URL authorizationServerUrl,
      URL resourceServerUrl,
      String walletClientId,
      String walletClientSecret
  ) {
    super(authorizationServerUrl, "/wallet", resourceServerUrl, walletClientId, walletClientSecret);
  }

}
