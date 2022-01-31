package no.ebax.sdk.baseclient.spi;

import no.ebax.sdk.baseclient.ApiClient;

public interface ApiClientProvider {
    ApiClient create(String baseurl);
}
