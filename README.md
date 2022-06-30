# Development kit for ePayment integration

The ePayment SDK provides reactive clients for interacting with the ePayment API using the Java Flow API.<br>
Currently there are no artifacts provided, so you would need to build them yourself using
>mvn install

and then importing them into your project.
## Usage

### Importing
#### Standard usage (Provided client)
By importing the specific client(s) (e.g. **merchant-client**) you need and the **webflux-client** module - Everything should work out of the box. 

#### Use your own http client
If you don't want to use our HTTP client implementation using webflux, you are free to provide your own. <br>
This SDK uses a Java Service Provider Interface. <br>
You need to implement [HttpClient](java/base-client/src/main/java/no/bankaxept/epayment/client/base/http/HttpClient.java) and [HttpClientProvider](java/base-client/src/main/java/no/bankaxept/epayment/client/base/spi/HttpClientProvider.java)
and then create a configuration file (like [this](java/webflux-client/src/main/resources/META-INF/services/no.bankaxept.epayment.client.base.spi.HttpClientProvider)). The [webflux-client module](java/webflux-client) can be used for reference

### Starting clients
The easiest way to create a client is to provide a baseclient.
You need a few values to get started
* **Base url** of the ePayment service
* **API-M key** (optional). Needed if you're routed through. If you're external, you probably always need this.
* **Strategy and credentials for fetching tokens** . The scheduled way is the one you usually want, unless you are testing or fetching tokens yourself.
> var merchantClient = new MerchantClient(new BaseClient.Builder(baseurl).apimKey(apimKey).withScheduledToken(username, password).build());
