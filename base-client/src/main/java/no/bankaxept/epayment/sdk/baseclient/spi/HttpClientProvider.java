package no.bankaxept.epayment.sdk.baseclient.spi;

import no.bankaxept.epayment.sdk.baseclient.HttpClient;

public interface HttpClientProvider {
    HttpClient create(String baseurl);
}
