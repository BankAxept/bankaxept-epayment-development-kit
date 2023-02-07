package no.bankaxept.epayment.client.base.http;

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
