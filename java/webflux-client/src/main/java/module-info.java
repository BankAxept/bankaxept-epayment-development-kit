import no.bankaxept.epayment.client.webflux.WebFluxHttpClientProvider;

module no.bankaxept.epayment.sdk.webfluxclient {
  requires reactor.core;
  requires no.bankaxept.epayment.sdk.baseclient;
  requires spring.webflux;
  requires spring.web;
  requires io.netty.resolver;
  requires io.netty.handler;
  requires reactor.netty.http;
  requires reactor.netty.core;
  provides no.bankaxept.epayment.client.base.spi.HttpClientProvider with WebFluxHttpClientProvider;
}
