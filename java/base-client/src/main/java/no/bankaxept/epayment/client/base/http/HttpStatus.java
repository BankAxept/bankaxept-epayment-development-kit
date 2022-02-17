package no.bankaxept.epayment.client.base.http;

import java.util.Objects;

public class HttpStatus {

    private final int status;

    public HttpStatus(int status) {
        if(status < 99 || status > 1000) throw new IllegalArgumentException("Unknown http status");
        this.status = status;
    }

    public boolean is2xxOk() {
        return startsWith(2);
    }

    public boolean is4xxClientError() {
        return startsWith(4);
    }

    public boolean is5xxServerError() {
        return startsWith(5);
    }

    private boolean startsWith(int firstDigit) {
        return status >= (firstDigit * 100) && status <= (firstDigit * 100) + 99;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HttpStatus that = (HttpStatus) o;
        return status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(status);
    }
}
