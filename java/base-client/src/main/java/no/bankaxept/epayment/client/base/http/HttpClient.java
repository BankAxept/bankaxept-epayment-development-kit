package no.bankaxept.epayment.client.base.http;


import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Publisher;

public interface HttpClient {
  Mono<HttpResponse> get(String uri, Map<String, List<String>> headers);

  Mono<HttpResponse> post(
      String uri,
      String body,
      Map<String, List<String>> headers
  );

  Mono<HttpResponse> delete(String uri, Map<String, List<String>> headers);

  Mono<HttpResponse> put(String uri, String body, Map<String, List<String>> headers);

}
