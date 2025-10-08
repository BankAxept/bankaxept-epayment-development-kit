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
import java.util.List;
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
        .assertNext(List::isEmpty)
        .verifyComplete();
  }

  private MappingBuilder MerchantEndpoint(ResponseDefinitionBuilder responseBuilder) {
    return get(urlPathEqualTo("/merchant"))
        .withHeader("Authorization", new EqualToPattern(bearerToken()))
        .willReturn(responseBuilder);
  }

  @Test
  public void wallet_certs_successful() {
    stubFor(WalletEndpoint(ok()));
    StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(client.getWalletCertificates()))
        .assertNext(List::isEmpty)
        .verifyComplete();
  }

  private MappingBuilder WalletEndpoint(ResponseDefinitionBuilder responseBuilder) {
    return get(urlPathEqualTo("/wallet"))
        .withHeader("Authorization", new EqualToPattern(bearerToken()))
        .willReturn(responseBuilder);
  }
}
