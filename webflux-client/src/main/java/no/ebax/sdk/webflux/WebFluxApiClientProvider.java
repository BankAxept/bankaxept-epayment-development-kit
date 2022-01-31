package no.ebax.sdk.webflux;


import no.ebax.sdk.baseclient.ApiClient;
import no.ebax.sdk.baseclient.spi.ApiClientProvider;

public class WebFluxApiClientProvider implements ApiClientProvider {

    @Override
    public ApiClient create(String baseurl) {
        return new WebFluxClient(baseurl);
    }
}
