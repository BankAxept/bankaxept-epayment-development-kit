package no.bankaxept.epayment.client.base;


import no.bankaxept.epayment.client.base.http.HttpResponse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow;

public interface HttpClient {
    /**
     *
     * @param uri
     * @param bodyPublisher can be null
     * @param headers
     * @return Publisher<HttpResponse> for post
     */
    Flow.Publisher<HttpResponse> post(String uri, Flow.Publisher<String> bodyPublisher, Map<String, List<String>> headers);

}
