package no.bankaxept.epayment.client.base.http;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import java.util.Objects;
import no.bankaxept.epayment.client.base.ClientError;
import no.bankaxept.epayment.client.base.RequestStatus;

public class HttpResponse {

  private final ObjectReader clientErrorReader = new ObjectMapper().readerFor(ProblemDetails.class);
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
    if (status.code() == 200) {
      return RequestStatus.Repeated;
    } else if (status.code() == 201) {
      return RequestStatus.Accepted;
    } else if (status.code() == 409) {
      return RequestStatus.Conflicted;
    } else if (status.code() == 422) {
      return RequestStatus.Rejected;
    } else if (status.is4xxClientError()) {
      throw parseClientError(body);
    } else {
      return RequestStatus.Failed;
    }
  }

  public String getBody() {
    return body;
  }

  private ClientError parseClientError(String body) {
    ProblemDetails details;
    try {
      details = clientErrorReader.readValue(body);
    } catch (JsonProcessingException e) {
      details = null;
    }
    if (details == null || details.detail() == null)
      return new ClientError(body);
    else
      return new ClientError(details.detail());
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

  @JsonIgnoreProperties(ignoreUnknown = true)
  record ProblemDetails(String detail) {}

}
