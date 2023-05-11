module no.bankaxept.epayment.sdk.baseclient {
  requires com.fasterxml.jackson.databind;
  requires com.fasterxml.jackson.datatype.jsr310;
  uses no.bankaxept.epayment.client.base.spi.HttpClientProvider;
  exports no.bankaxept.epayment.client.base;
  exports no.bankaxept.epayment.client.base.spi;
  exports no.bankaxept.epayment.client.base.http;
}
