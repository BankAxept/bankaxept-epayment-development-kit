package no.bankaxept.epayment.sdk.baseclient;

import java.util.Objects;

public class AccessTokenResponse {
    private String tokenType;
    private Integer expiresIn;
    private Long expiresOn;
    private Long notBefore;
    private String accessToken;

    public AccessTokenResponse() {
    }

    public AccessTokenResponse tokenType(String tokenType) {
        this.tokenType = tokenType;
        return this;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public AccessTokenResponse expiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
        return this;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
    }


    public AccessTokenResponse expiresOn(Long expiresOn) {
        this.expiresOn = expiresOn;
        return this;
    }

    public Long getExpiresOn() {
        return expiresOn;
    }

    public void setExpiresOn(Long expiresOn) {
        this.expiresOn = expiresOn;
    }


    public AccessTokenResponse notBefore(Long notBefore) {
        this.notBefore = notBefore;
        return this;
    }

    public Long getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(Long notBefore) {
        this.notBefore = notBefore;
    }

    public AccessTokenResponse accessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
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
        return Objects.equals(this.tokenType, accessTokenResponse.tokenType) &&
                Objects.equals(this.expiresIn, accessTokenResponse.expiresIn) &&
                Objects.equals(this.expiresOn, accessTokenResponse.expiresOn) &&
                Objects.equals(this.notBefore, accessTokenResponse.notBefore) &&
                Objects.equals(this.accessToken, accessTokenResponse.accessToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tokenType, expiresIn, expiresOn, notBefore, accessToken);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AccessTokenResponse {\n");
        sb.append("    tokenType: ").append(toIndentedString(tokenType)).append("\n");
        sb.append("    expiresIn: ").append(toIndentedString(expiresIn)).append("\n");
        sb.append("    expiresOn: ").append(toIndentedString(expiresOn)).append("\n");
        sb.append("    notBefore: ").append(toIndentedString(notBefore)).append("\n");
        sb.append("    accessToken: ").append(toIndentedString(accessToken)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}

