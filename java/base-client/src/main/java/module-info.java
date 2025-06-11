module no.bankaxept.epayment.sdk.baseclient {
  uses no.bankaxept.epayment.client.base.spi.HttpClientProvider;
  requires com.fasterxml.jackson.annotation;
  requires com.fasterxml.jackson.databind;
  requires reactor.core;
  requires org.reactivestreams;
  exports no.bankaxept.epayment.client.base;
  exports no.bankaxept.epayment.client.base.spi;
  exports no.bankaxept.epayment.client.base.http;
  exports no.bankaxept.epayment.client.base.accesstoken;
}
