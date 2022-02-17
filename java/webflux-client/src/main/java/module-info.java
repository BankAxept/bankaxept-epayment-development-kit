import no.bankaxept.epayment.client.webflux.WebFluxHttpClientProvider;

module no.bankaxept.epayment.sdk.webfluxclient {
    requires org.reactivestreams;
    requires reactor.core;
    requires spring.web;
    requires no.bankaxept.epayment.sdk.baseclient;
    requires spring.webflux;
    provides no.bankaxept.epayment.client.base.spi.HttpClientProvider with WebFluxHttpClientProvider;
}