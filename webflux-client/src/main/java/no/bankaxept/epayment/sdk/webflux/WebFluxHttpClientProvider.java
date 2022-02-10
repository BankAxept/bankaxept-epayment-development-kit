package no.bankaxept.epayment.sdk.webflux;

import no.bankaxept.epayment.sdk.baseclient.HttpClient;
import no.bankaxept.epayment.sdk.baseclient.spi.HttpClientProvider;

public class WebFluxHttpClientProvider implements HttpClientProvider {
    @Override
    public HttpClient create(String baseurl) {
        return new WebFluxClient(baseurl);
    }
}
