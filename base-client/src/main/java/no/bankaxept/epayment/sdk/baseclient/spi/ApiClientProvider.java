package no.bankaxept.epayment.sdk.baseclient.spi;

import no.bankaxept.epayment.sdk.baseclient.ApiClient;

public interface ApiClientProvider {
    ApiClient create(String baseurl);
}
