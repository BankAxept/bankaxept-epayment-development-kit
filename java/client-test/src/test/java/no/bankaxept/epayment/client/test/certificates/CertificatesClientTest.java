package no.bankaxept.epayment.client.test.certificates;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import java.net.MalformedURLException;
import java.util.List;
import no.bankaxept.epayment.client.certificates.MerchantCertificatesClient;
import no.bankaxept.epayment.client.certificates.WalletCertificatesClient;
import no.bankaxept.epayment.client.test.AbstractBaseClientWireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.adapter.JdkFlowAdapter;
import reactor.test.StepVerifier;

public class CertificatesClientTest extends AbstractBaseClientWireMockTest {

  //TODO: add a test where you mock some certificate
  private static final String JSON_EMPTY_LIST = "[]";
  private MerchantCertificatesClient merchantClient;
  private WalletCertificatesClient walletClient;

  @BeforeEach
  public void setup(WireMockRuntimeInfo wmRuntimeInfo) throws MalformedURLException {
    super.setup(wmRuntimeInfo);
    merchantClient = new MerchantCertificatesClient(baseClient);
    walletClient = new WalletCertificatesClient(baseClient);
  }

  @Test
  public void merchantCertsEmptyOk() {
    stubFor(merchantEndpoint(okJson(JSON_EMPTY_LIST)));
    StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(merchantClient.getCertificates()))
        .assertNext(List::isEmpty)
        .verifyComplete();
  }

  private MappingBuilder merchantEndpoint(ResponseDefinitionBuilder responseBuilder) {
    return get(urlPathEqualTo("/merchant"))
        .withHeader("Authorization", new EqualToPattern(bearerToken()))
        .willReturn(responseBuilder);
  }

  @Test
  public void walletCertsEmptyOk() {
    stubFor(walletEndpoint(okJson(JSON_EMPTY_LIST)));
    StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(walletClient.getCertificates()))
        .assertNext(List::isEmpty)
        .verifyComplete();
  }

  private MappingBuilder walletEndpoint(ResponseDefinitionBuilder responseBuilder) {
    return get(urlPathEqualTo("/wallet"))
        .withHeader("Authorization", new EqualToPattern(bearerToken()))
        .willReturn(responseBuilder);
  }
}
