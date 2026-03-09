package no.bankaxept.epayment.client.base.spi;

import no.bankaxept.epayment.client.base.http.HttpClient;
import no.bankaxept.epayment.client.base.http.HttpResponse;
import java.util.concurrent.Flow.Publisher;
import java.util.function.Function;

public interface HttpClientProvider {

  HttpClient create(String baseurl, Function<Publisher<HttpResponse>, Publisher<HttpResponse>> transformer);
}
