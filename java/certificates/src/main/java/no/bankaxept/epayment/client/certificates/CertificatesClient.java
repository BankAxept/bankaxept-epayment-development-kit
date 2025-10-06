package no.bankaxept.epayment.client.certificates;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import no.bankaxept.epayment.client.base.BaseClient;
import no.bankaxept.epayment.client.base.MapOperator;
import no.bankaxept.epayment.client.base.RequestStatus;
import no.bankaxept.epayment.client.base.http.HttpResponse;

public class CertificatesClient {

  private final BaseClient baseClient;

  public CertificatesClient(BaseClient baseClient) {
    this.baseClient = baseClient;
  }

  private final Executor executor = Executors.newSingleThreadExecutor();

  public CertificatesClient(URL authorizationServerUrl, URL resourceServerUrl, String clientId, String clientSecret) {
    this(
        new BaseClient.Builder(resourceServerUrl)
            .withScheduledToken(authorizationServerUrl, clientId, clientSecret)
            .build()
    );
  }

  public Flow.Publisher<RequestStatus> getMerchantCertificates() {
    var emptyHeaders = new HashMap<String, List<String>>();
    return new MapOperator<>(
        baseClient.get(
            "/certificates/merchant",
            emptyHeaders
        ),
        HttpResponse::requestStatus
    );
  }
}
