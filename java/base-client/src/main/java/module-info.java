module no.bankaxept.epayment.sdk.baseclient {
  uses no.bankaxept.epayment.client.base.spi.HttpClientProvider;
  requires com.fasterxml.jackson.annotation;
  requires com.fasterxml.jackson.databind;
  requires reactor.core;
  exports no.bankaxept.epayment.client.base;
  exports no.bankaxept.epayment.client.base.spi;
  exports no.bankaxept.epayment.client.base.http;
}
