package no.bankaxept.epayment.client.base.http;


import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow;

public interface HttpClient {
    Flow.Publisher<HttpResponse> post(String uri, Flow.Publisher<String> bodyPublisher, Map<String, List<String>> headers);
}
