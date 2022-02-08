package no.bankaxept.epayment.sdk.baseclient;

import java.util.Objects;

public class AccessTokenResponse {
    private Long expiresOn;
    private String accessToken;

    private AccessTokenResponse() {
    }

    public AccessTokenResponse(Long expiresOn, String accessToken) {
        this.expiresOn = expiresOn;
        this.accessToken = accessToken;
    }

    public Long getExpiresOn() {
        return expiresOn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccessTokenResponse accessTokenResponse = (AccessTokenResponse) o;
        return  Objects.equals(this.expiresOn, accessTokenResponse.expiresOn) &&
                Objects.equals(this.accessToken, accessTokenResponse.accessToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expiresOn, accessToken);
    }

    @Override
    public String toString() {
        return "class AccessTokenResponse {\n" +
                "    expiresOn: " + toIndentedString(expiresOn) + "\n" +
                "    accessToken: " + toIndentedString(accessToken) + "\n" +
                "}";
    }

    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}

