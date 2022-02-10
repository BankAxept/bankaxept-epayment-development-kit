import no.bankaxept.epayment.sdk.webflux.WebFluxHttpClientProvider;

module no.bankaxept.epayment.sdk.webfluxclient {
    requires org.reactivestreams;
    requires reactor.core;
    requires spring.web;
    requires no.bankaxept.epayment.sdk.baseclient;
    requires spring.webflux;
    provides no.bankaxept.epayment.sdk.baseclient.spi.HttpClientProvider with WebFluxHttpClientProvider;
}