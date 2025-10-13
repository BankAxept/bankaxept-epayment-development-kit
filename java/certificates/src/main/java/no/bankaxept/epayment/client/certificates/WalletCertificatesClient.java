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
      String clientId,
      String clientSecret
  ) {
    this(new BaseClient.Builder(resourceServerUrl)
        .withScheduledToken(authorizationServerUrl, clientId, clientSecret)
        .build());
  }

}
