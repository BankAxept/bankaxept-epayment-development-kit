package no.bankaxept.epayment.client.certificates;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow;
import no.bankaxept.epayment.client.base.BaseClient;
import no.bankaxept.epayment.client.base.MapOperator;
import no.bankaxept.epayment.client.base.http.HttpResponse;
import no.bankaxept.epayment.client.certificates.bankaxept.CertificateData;

public class CertificatesClient {

  private final BaseClient baseClient;

  public CertificatesClient(BaseClient baseClient) {
    this.baseClient = baseClient;
  }

  public CertificatesClient(URL authorizationServerUrl, URL resourceServerUrl, String clientId, String clientSecret) {
    this(
        new BaseClient.Builder(resourceServerUrl)
            .withScheduledToken(authorizationServerUrl, clientId, clientSecret)
            .build()
    );
  }

  public Flow.Publisher<List<CertificateData>> getMerchantCertificates() {
    return new MapOperator<>(
        baseClient.get(
            "/merchant",
            Map.of()
        ),
        HttpResponse::getListBody
    );
  }

  public Flow.Publisher<List<CertificateData>> getWalletCertificates() {
    return new MapOperator<>(
        baseClient.get(
            "/wallet",
            Map.of()
        ),
        HttpResponse::getListBody
    );
  }
}
