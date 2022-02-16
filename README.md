# Development kit for ePayment integration

This sdk provides a spi.

To use it, you can implement [HttpClient](base-client/src/main/java/no/bankaxept/epayment/sdk/baseclient/HttpClient.java) and [HttpClientProvider](base-client/src/main/java/no/bankaxept/epayment/sdk/baseclient/spi/HttpClientProvider.java)

You can also use the provided [Webflux client](webflux-client) directly. 