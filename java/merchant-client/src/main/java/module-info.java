module no.bankaxept.epayment.sdk.merchant {
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.databind;
  requires com.fasterxml.jackson.datatype.jsr310;
  requires jakarta.annotation;
  requires no.bankaxept.epayment.sdk.baseclient;

  exports no.bankaxept.epayment.client.merchant;
}
