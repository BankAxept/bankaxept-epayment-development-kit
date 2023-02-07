module no.bankaxept.epayment.sdk.tokenrequestor {
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.databind;
  requires com.fasterxml.jackson.datatype.jsr310;
  requires no.bankaxept.epayment.sdk.baseclient;
  requires swagger.annotations;
  requires java.compiler;
  requires jsr305;

  exports no.bankaxept.epayment.client.tokenrequestor;
}
