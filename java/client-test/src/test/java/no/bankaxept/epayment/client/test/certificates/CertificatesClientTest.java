package no.bankaxept.epayment.client.test.certificates;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import java.net.MalformedURLException;
import no.bankaxept.epayment.client.base.RequestStatus;
import no.bankaxept.epayment.client.certificates.CertificatesClient;
import no.bankaxept.epayment.client.test.AbstractBaseClientWireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.adapter.JdkFlowAdapter;
import reactor.test.StepVerifier;

public class CertificatesClientTest extends AbstractBaseClientWireMockTest {

  private CertificatesClient client;

  @BeforeEach
  public void setup(WireMockRuntimeInfo wmRuntimeInfo) throws MalformedURLException {
    super.setup(wmRuntimeInfo);
    client = new CertificatesClient(baseClient);
  }

  @Test
  public void merchant_certs_successful() {
    stubFor(MerchantEndpoint(ok()));
    StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(client.getMerchantCertificates()))
        .expectNext(RequestStatus.Accepted);
  }

  private MappingBuilder MerchantEndpoint(ResponseDefinitionBuilder responseBuilder) {
    return get(urlPathEqualTo("/certificates/merchant"))
        .withHeader("Authorization", new EqualToPattern(bearerToken()))
        .willReturn(responseBuilder);
  }
}
