# Development kit for ePayment integration

This sdk provides a spi.

To use it, you can implement [HttpClient](java/base-client/src/main/java/no/bankaxept/epayment/client/base/HttpClient.java) and [HttpClientProvider](java/base-client/src/main/java/no/bankaxept/epayment/client/base/spi/HttpClientProvider.java)

You can also use the provided [Webflux client](java/webflux-client) directly. See [tests](java/webflux-client/src/test/java/no/bankaxept/epayment/client/webflux/WebFluxClientTest.java) for some examples