package no.bankaxept.epayment.client.base.http;

import no.bankaxept.epayment.client.base.RequestStatus;
import java.util.Objects;

public class HttpResponse {

  private final HttpStatus status;
  private final String body;

  public HttpResponse(int status, String body) {
    this.status = new HttpStatus(status);
    this.body = body;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public RequestStatus requestStatus() {
    var requestStatus = switch (status.code()) {
      case 200 -> RequestStatus.Repeated;
      case 201 -> RequestStatus.Accepted;
      case 422 -> RequestStatus.Rejected;
      case 409 -> RequestStatus.Conflicted;
      default -> status.is4xxClientError() ? RequestStatus.ClientError : RequestStatus.Failed;
    };
    if (requestStatus == RequestStatus.ClientError) {
      throw new IllegalArgumentException(body);
    } else {
      return requestStatus;
    }
  }

  public String getBody() {
    return body;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    HttpResponse that = (HttpResponse) o;
    return Objects.equals(status, that.status) && Objects.equals(body, that.body);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, body);
  }


}
