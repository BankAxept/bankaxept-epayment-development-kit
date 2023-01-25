package no.bankaxept.epayment.client.base.accesstoken;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;

class AccessToken {
    private final String token;
    private final Instant expiry;

    public AccessToken(String token, long expiresIn, Clock clock) {
        this.token = token;
        this.expiry = clock.instant().plusSeconds(expiresIn);
    }

    public AccessToken(String token, long expiresIn) {
        this(token, expiresIn, Clock.systemDefaultZone());
    }

    static AccessToken parse(String input) {
        return Parser.parse(input);
    }

    static AccessToken parse(String input, Clock clock) {
        return Parser.parse(input, clock);
    }


    public String getToken() {
        return token;
    }

    public Instant getExpiry() {
        return expiry;
    }

    public long secondsUntilTenMinutesBeforeExpiry(Clock clock) {
        Instant tenMinutesBeforeExpiry = expiry.minus(10, ChronoUnit.MINUTES);
        return Duration.between(clock.instant(), tenMinutesBeforeExpiry).toSeconds();
    }

    private static class Parser {
        private static final Pattern tokenPattern = Pattern.compile("\"access_token\"\\s*:\\s*\"(.*)\"");
        private static final Pattern expiryPattern = Pattern.compile("\"expires_in\"\\s*:\\s*(\\d+)");

        static AccessToken parse(String input) {
            return parse(input, Clock.systemDefaultZone());
        }

        static AccessToken parse(String input, Clock clock) {
            var tokenMatcher = tokenPattern.matcher(input);
            var expiryMatcher = expiryPattern.matcher(input);
            if (!tokenMatcher.find() || !expiryMatcher.find()) {
                throw new IllegalArgumentException("Could not parse token or expiry");
            }
            return new AccessToken(tokenMatcher.group(1), Long.parseLong(expiryMatcher.group(1)), clock);
        }
    }
}
