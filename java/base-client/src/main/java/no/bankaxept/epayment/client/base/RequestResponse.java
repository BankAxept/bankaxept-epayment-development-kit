package no.bankaxept.epayment.client.base;

import java.util.Objects;

public class RequestResponse<T> {
    private final RequestStatus status;
    private final T response;

    public RequestResponse(RequestStatus status, T response) {
        this.status = status;
        this.response = response;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public T getResponse() {
        return response;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestResponse<?> that = (RequestResponse<?>) o;
        return status == that.status && Objects.equals(response,that.response);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, response);
    }
}
