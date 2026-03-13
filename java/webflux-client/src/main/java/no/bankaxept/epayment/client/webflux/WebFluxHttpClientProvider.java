package no.bankaxept.epayment.client.webflux;

import static reactor.adapter.JdkFlowAdapter.flowPublisherToFlux;
import static reactor.adapter.JdkFlowAdapter.publisherToFlowPublisher;

import java.util.concurrent.Flow.Publisher;
import java.util.function.Function;
import no.bankaxept.epayment.client.base.http.HttpClient;
import no.bankaxept.epayment.client.base.http.HttpResponse;
import no.bankaxept.epayment.client.base.spi.HttpClientProvider;

public class WebFluxHttpClientProvider implements HttpClientProvider {

  @Override
  public HttpClient create(String baseurl, Function<Publisher<HttpResponse>, Publisher<HttpResponse>> transformer) {
    return new WebFluxClient(
        baseurl,
        response -> flowPublisherToFlux(transformer.apply(publisherToFlowPublisher(response))).single()
    );
  }
}
