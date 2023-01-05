package no.bankaxept.epayment.client.base.accesstoken;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.regex.Pattern;

class AccessToken {
    private String input;
    private final String token;
    private final Instant expiry;
    private Integer expirySecondsFromStart;

    public AccessToken(String token, Instant expiry, String input) {
        this.token = token;
        this.expiry = expiry;
        this.input = input;
    }

    public AccessToken(String token, Integer expirySecondsFromStart, String input) {
        this.token = token;
        this.expiry = Instant.now().plusSeconds(expirySecondsFromStart);
        this.expirySecondsFromStart = expirySecondsFromStart;
        this.input = input;
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

    public Integer getExpirySecondsFromStart() {
        return expirySecondsFromStart;
    }

    public String getInput() {
        return input;
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

        static AccessToken parse(String input) {
            var tokenMatcher = tokenPattern.matcher(input);
            var expiryMatcher = expiryPattern.matcher(input);
            var expirySecondsFromStartMatcher = expirySecondsFromStartPattern.matcher(input);
            if ( !tokenMatcher.find()) {
                throw new IllegalArgumentException("Could not parse token: " + input); //onError
            }
            if (expirySecondsFromStartMatcher.find()) {
                return new AccessToken(tokenMatcher.group(1), Integer.parseInt(expirySecondsFromStartMatcher.group(1)), input);
            }
            if(expiryMatcher.find()) {
                return new AccessToken(tokenMatcher.group(1), Instant.ofEpochMilli(Long.parseLong(expiryMatcher.group(1))), input);
            }
            throw new IllegalArgumentException("Could not parse expiry: " + input); //onError
        }

    }
}
