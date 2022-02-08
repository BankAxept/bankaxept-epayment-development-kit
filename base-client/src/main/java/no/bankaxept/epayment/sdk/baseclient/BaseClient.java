package no.bankaxept.epayment.sdk.baseclient;

import no.bankaxept.epayment.sdk.baseclient.spi.HttpClientProvider;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Flow;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseClient {

    private Supplier<String> accessTokenSupplier;
    private HttpClient httpClient;

    public BaseClient(String baseurl, String apimKey, String username, String password) {
        httpClient = ServiceLoader.load(HttpClientProvider.class)
                .findFirst()
                .map(httpClientProvider -> httpClientProvider.create(baseurl))
                .orElseThrow();
        this.accessTokenSupplier = new AccessTokenSupplier("/token", apimKey, username, password);
    }

    public Flow.Publisher<String> post(
            String uri,
            Flow.Publisher<String> body,
            String correlationId
    ) {
        return post(uri, body, correlationId, Collections.emptyMap());
    }

    public Flow.Publisher<String> post(
            String uri,
            Flow.Publisher<String> body,
            String correlationId,
            Map<String, List<String>> headers
    ) {
        var allHeaders = new HashMap<>(headers);
        allHeaders.put("X-Correlation-Id", Collections.singletonList(correlationId));
        allHeaders.put("Authorization", Collections.singletonList("Bearer " + accessTokenSupplier.get()));
        return httpClient.post(uri, body, allHeaders);
    }

    private class AccessTokenSupplier implements Supplier<String>, Flow.Subscriber<String> {
        private final Pattern tokenPattern = Pattern.compile("\"accessToken\"\\s*:\\s*\".*\"");
        private final Pattern expiryPattern = Pattern.compile("\"expiresOn\"\\s*:\\s*\\d+");

        private final String uri;
        private final String apimKey;
        private String username;
        private String password;

        private String token;
        private Date expiry;

        private boolean waiting = false;

        public AccessTokenSupplier(String uri, String apimKey, String username, String password) {
            this.uri = uri;
            this.apimKey = apimKey;
            this.username = username;
            this.password = password;
        }

        @Override
        public synchronized String get() {
            if (isExpired()) fetchNewToken();
            try {
                while(waiting) Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return token;
        }

        private void fetchNewToken() {
            httpClient.post(uri, null, createHeaders()).subscribe(this);
        }

        private HashMap<String, List<String>> createHeaders() {
            var headers = new LinkedHashMap<String, List<String>>();
            headers.put("Ocp-Apim-Subscription-Key", Collections.singletonList(apimKey));
            headers.put("Authorization", Collections.singletonList(authenticationHeader()));
            return headers;
        }

        private String authenticationHeader() {
            return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        }

        private boolean isExpired() {
            return token == null || expiry.after(fiveMinutesFromNow());
        }

        private Date fiveMinutesFromNow() {
            return Date.from(Instant.now().plus(Duration.ofMinutes(5)));
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            waiting = true;
            subscription.request(1);
        }

        @Override
        public void onNext(String item) {
            Matcher tokenMatcher = tokenPattern.matcher(item);
            Matcher expiryMatcher = expiryPattern.matcher(item);
            if(!tokenMatcher.find() || !expiryMatcher.find()) throw new IllegalStateException();
            this.token = tokenMatcher.group().split(":")[1].split("\"")[1];
            this.expiry = new Date(Long.parseLong(expiryMatcher.group().split(":")[1].trim()));
        }

        @Override
        public void onError(Throwable throwable) {
            waiting = false;
            throw new IllegalStateException(throwable);
        }

        @Override
        public void onComplete() {
            waiting = false;
        }
    }

}
