package no.bankaxept.epayment.client.webflux;

import no.bankaxept.epayment.client.base.http.HttpClient;
import no.bankaxept.epayment.client.base.spi.HttpClientProvider;

public class WebFluxHttpClientProvider implements HttpClientProvider {
    @Override
    public HttpClient create(String baseurl) {
        return new WebFluxClient(baseurl);
    }
}
