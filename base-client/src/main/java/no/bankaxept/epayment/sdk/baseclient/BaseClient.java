package no.bankaxept.epayment.sdk.baseclient;

import no.bankaxept.epayment.sdk.baseclient.spi.HttpClientProvider;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseClient implements Flow.Subscriber<String> {

    private String token;
    private HttpClient httpClient;

    public BaseClient(String baseurl, String apimKey, String username, String password) {
        httpClient = ServiceLoader.load(HttpClientProvider.class)
                .findFirst()
                .map(httpClientProvider -> httpClientProvider.create(baseurl))
                .orElseThrow();
        new AccessTokenSupplier("/token", apimKey, username, password).subscribe(this);
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
        waitUntilReady();
        var allHeaders = new HashMap<>(headers);
        allHeaders.put("X-Correlation-Id", Collections.singletonList(correlationId));
        allHeaders.put("Authorization", Collections.singletonList("Bearer " + token));
        return httpClient.post(uri, body, allHeaders);
    }

    private void waitUntilReady() { //TODO how to get rid of this..
        while(token == null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(String item) {
        this.token = item;
    }

    @Override
    public void onError(Throwable throwable) { }

    @Override
    public void onComplete() { }

    private class AccessTokenSupplier extends SubmissionPublisher<String> implements Flow.Processor<String, String> {
        private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        private final Pattern tokenPattern = Pattern.compile("\"accessToken\"\\s*:\\s*\".*\"");
        private final Pattern expiryPattern = Pattern.compile("\"expiresOn\"\\s*:\\s*\\d+");

        private final String uri;
        private final String apimKey;
        private String username;
        private String password;

        private long expiry;

        public AccessTokenSupplier(String uri, String apimKey, String username, String password) {
            this.uri = uri;
            this.apimKey = apimKey;
            this.username = username;
            this.password = password;
            fetchNewToken();
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


        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(String item) {
            Matcher tokenMatcher = tokenPattern.matcher(item);
            Matcher expiryMatcher = expiryPattern.matcher(item);
            if (!tokenMatcher.find() || !expiryMatcher.find()) throw new IllegalStateException("Could not parse token or expiry");
            this.expiry = Long.parseLong(expiryMatcher.group().split(":")[1].trim());
            submit(tokenMatcher.group().split(":")[1].split("\"")[1]);
        }

        @Override
        public void onError(Throwable throwable) {
            throw new IllegalStateException("Error when fetching token", throwable);
        }

        @Override
        public void onComplete() {
            scheduler.schedule(this::fetchNewToken, tenMinutesBeforeExpiry(), TimeUnit.MILLISECONDS);
        }

        private long tenMinutesBeforeExpiry() {
            return expiry - Instant.now().minus(10, ChronoUnit.MINUTES).toEpochMilli();
        }
    }

}
