package no.bankaxept.epayment.client.base.http;

import java.util.Objects;

public class HttpStatus {

  private final int code;

  public HttpStatus(int code) {
    if (code < 99 || code > 1000)
      throw new IllegalArgumentException("Illegal HTTP status code: " + code);
    this.code = code;
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

  public int code() {
    return code;
  }

  private boolean startsWith(int firstDigit) {
    return code >= (firstDigit * 100) && code <= (firstDigit * 100) + 99;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    HttpStatus that = (HttpStatus) o;
    return code == that.code;
  }

  @Override
  public int hashCode() {
    return Objects.hash(code);
  }

  @Override
  public String toString() {
    return String.valueOf(code);
  }

}
