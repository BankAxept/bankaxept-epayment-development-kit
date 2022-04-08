package no.bankaxept.epayment.client.base;

public class AccessFailed extends RuntimeException {
    public AccessFailed(Throwable cause) {
        super(cause);
    }
}
