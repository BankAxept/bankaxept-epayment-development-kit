package no.bankaxept.epayment.client.base;

public class ClientError extends RuntimeException {

  public ClientError(String message) {
    super(message);
  }

}
