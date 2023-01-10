package no.bankaxept.epayment.client.base.accesstoken;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.regex.Pattern;

class AccessToken {
    private final String token;
    private final Instant expiry;
    private Integer expirySecondsFromStart;

    public AccessToken(String token, Instant expiry) {
        this.token = token;
        this.expiry = expiry;
    }

    public AccessToken(String token, Integer expirySecondsFromStart, Clock clock) {
        this.token = token;
        this.expiry = clock.instant().plusSeconds(expirySecondsFromStart);
        this.expirySecondsFromStart = expirySecondsFromStart;
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

    public Integer getExpirySecondsFromStart() {
        return expirySecondsFromStart;
    }


    public long tenSecondsBeforeExpiry(Clock clock) {
        if(expirySecondsFromStart != null) {
            return (expirySecondsFromStart * 1000) - 1000;
        }
        return Duration.between(clock.instant(), expiry.minusSeconds(10)).toMillis();
    }

    private static class Parser {
        private static final Pattern tokenPattern = Pattern.compile("\"access_token\"\\s*:\\s*\"(.*?)\"");
        private static final Pattern expiryPattern = Pattern.compile("\"expires_on\"\\s*:\\s*(\\d+)");
        private static final Pattern expirySecondsFromStartPattern = Pattern.compile("\"expires_in\"\\s*:\\s*(\\d+)");

        static AccessToken parse(String input, Clock clock) {
            var tokenMatcher = tokenPattern.matcher(input);
            var expiryMatcher = expiryPattern.matcher(input);
            var expirySecondsFromStartMatcher = expirySecondsFromStartPattern.matcher(input);
            if ( !tokenMatcher.find()) {
                throw new IllegalArgumentException("Could not parse token");
            }
            if (expirySecondsFromStartMatcher.find()) {
                return new AccessToken(tokenMatcher.group(1), Integer.parseInt(expirySecondsFromStartMatcher.group(1)), clock);
            }
            if(expiryMatcher.find()) {
                return new AccessToken(tokenMatcher.group(1), Instant.ofEpochSecond(Long.parseLong(expiryMatcher.group(1))));
            }
            throw new IllegalArgumentException("Could not parse expiry");
        }

    }
}
