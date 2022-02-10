package no.bankaxept.epayment.sdk.baseclient;


import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow;

public interface HttpClient {
    Flow.Publisher<ClientResponse> post(String uri, Flow.Publisher<String> bodyPublisher, Map<String, List<String>> headers);
}
