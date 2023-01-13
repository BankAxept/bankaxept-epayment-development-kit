package no.bankaxept.epayment.client.accesstoken;

import no.bankaxept.epayment.client.base.accesstoken.AccessTokenPublisher;
import no.bankaxept.epayment.client.base.accesstoken.AccessTokenSubscriber;
import no.bankaxept.epayment.client.base.accesstoken.GrantType;
import no.bankaxept.epayment.client.base.accesstoken.ScheduledAccessTokenPublisher;
import no.bankaxept.epayment.client.base.accesstoken.Scope;
import no.bankaxept.epayment.client.webflux.WebFluxClient;

import java.time.Duration;
import java.util.List;

public class AccessTokenRetriever {

    private final AccessTokenPublisher publisher;

    public AccessTokenRetriever(String uri, String id, String secret, GrantType grantType, List<Scope> scopes) {
        publisher = new ScheduledAccessTokenPublisher.Builder()
                .httpClient(new WebFluxClient())
                .uri(uri)
                .credentials(id, secret)
                .grantType(grantType)
                .scopes(scopes)
                .build();
    }


    public AccessTokenRetriever(String uri, String id, String secret, GrantType grantType, Scope scope) {
        this(uri, id, secret, grantType, List.of(scope));
    }

    public String get() {
        return new AccessTokenSubscriber(publisher).get(Duration.ofSeconds(4));
    }

    public String get(Duration timeout) {
        return new AccessTokenSubscriber(publisher).get(timeout);
    }
}
