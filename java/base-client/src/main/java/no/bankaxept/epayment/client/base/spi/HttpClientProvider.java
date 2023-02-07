package no.bankaxept.epayment.client.base.spi;

import no.bankaxept.epayment.client.base.http.HttpClient;

public interface HttpClientProvider {

  HttpClient create(String baseurl);
}
