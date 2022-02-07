package no.bankaxept.epayment.sdk.webflux;

import no.bankaxept.epayment.sdk.baseclient.ApiClient;
import no.bankaxept.epayment.sdk.baseclient.spi.ApiClientProvider;

public class WebFluxApiClientProvider implements ApiClientProvider {
    @Override
    public ApiClient create(String baseurl) {
        return new WebFluxClient(baseurl);
    }
}
