package no.bankaxept.epayment.client.certificates;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.bankaxept.epayment.client.base.BaseClient;
import no.bankaxept.epayment.client.base.MapOperator;
import no.bankaxept.epayment.client.base.http.HttpResponse;
import no.bankaxept.epayment.client.certificates.bankaxept.CertificateData;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow;

public sealed abstract class AbstractCertificatesClient permits MerchantCertificatesClient, WalletCertificatesClient {

  private final ObjectMapper objectMapper = new ObjectMapper()
      .registerModule(new JavaTimeModule())
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  private final String endpoint;
  private final BaseClient baseClient;

  public AbstractCertificatesClient(BaseClient baseClient, String endpoint) {
    this.baseClient = baseClient;
    this.endpoint = endpoint;
  }

  public AbstractCertificatesClient(URL authorizationServerUrl, String endpoint, URL resourceServerUrl, String clientId, String clientSecret) {
    this(
        new BaseClient.Builder(resourceServerUrl)
            .withScheduledToken(authorizationServerUrl, clientId, clientSecret)
            .build(),
        endpoint
    );
  }

  public Flow.Publisher<List<CertificateData>> getCertificates() {
    return new MapOperator<>(
        baseClient.get(
            endpoint,
            Map.of()
        ),
        this::responseToBody
    );
  }

  protected List<CertificateData> responseToBody(HttpResponse response) {
    try {
      return objectMapper.readerForListOf(CertificateData.class).readValue(response.getBody());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
