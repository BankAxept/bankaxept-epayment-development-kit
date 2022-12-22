package no.bankaxept.epayment.client.base.accesstoken;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.regex.Pattern;

class AccessToken {
    private final String token;
    private final Instant expiry;

    public AccessToken(String token, Instant expiry) {
        this.token = token;
        this.expiry = expiry;
    }

    static AccessToken parse(String input) {
        return Parser.parse(input);
    }


    public String getToken() {
        return token;
    }

    public Instant getExpiry() {
        return expiry;
    }

    public long millisUntilSecondsBeforeExpiry(Clock clock) {
        return Duration.between(clock.instant().plusSeconds(10), expiry).toMillis();
    }

    private static class Parser {
        private static final Pattern tokenPattern = Pattern.compile("\"access_token\"\\s*:\\s*\"(.*)\"");
        private static final Pattern expiryPattern = Pattern.compile("\"expires_on\"\\s*:\\s*(\\d+)");

        static AccessToken parse(String input) {
            var tokenMatcher = tokenPattern.matcher(input);
            var expiryMatcher = expiryPattern.matcher(input);
            if (!tokenMatcher.find() || !expiryMatcher.find()) {
                throw new IllegalArgumentException("Could not parse token or expiry: " + input); //onError
            }
            return new AccessToken(tokenMatcher.group(1), Instant.ofEpochMilli(Long.parseLong(expiryMatcher.group(1))));
        }

    }
}
