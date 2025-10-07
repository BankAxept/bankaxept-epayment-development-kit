package no.bankaxept.epayment.client.test.certificates;

import no.bankaxept.epayment.client.certificates.CertificatesClient;
import java.net.MalformedURLException;
import java.net.URL;

public class CertificatesClientIT {
  private CertificatesClient testClient() throws MalformedURLException {
    return new CertificatesClient(
        new URL(System.getenv("AUTHORIZATION_SERVER_URL")),
        new URL(System.getenv("CERTIFICATES_API_URL")),
        System.getenv("CLIENT_ID"),
        System.getenv("CLIENT_SECRET")
    );
  }
}
