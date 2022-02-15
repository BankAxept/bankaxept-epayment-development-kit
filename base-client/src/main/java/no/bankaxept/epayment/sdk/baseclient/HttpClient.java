package no.bankaxept.epayment.sdk.baseclient;


import no.bankaxept.epayment.sdk.baseclient.http.HttpResponse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow;

public interface HttpClient {
    /***
     * Should handle null or empty bodyPublisher as no body
     ***/
    Flow.Publisher<HttpResponse> post(String uri, Flow.Publisher<String> bodyPublisher, Map<String, List<String>> headers);

}
