# Development kit for BankAxept ePayment integration

The BankAxept ePayment development kit provides OpenAPI specifications and Java clients for integration with the
ePayment APIs:
* Merchant API
* Token Requestor API
* Wallet API

## Documentation overview

On our publicly hosted [GitHub Pages](https://epayment.bankaxept.no/getting_started/) you may peruse a hand crafted overview of our documentation.

## Maven artifacts for Java clients

There are three Maven artifacts available, one for each if the ePayment APIs:
```xml
<dependency>
  <groupId>no.bankaxept.epayment</groupId>
  <artifactId>merchant-client</artifactId>
  <version>x.y.z</version>
</dependency>

<dependency>
  <groupId>no.bankaxept.epayment</groupId>
  <artifactId>token-requestor-client</artifactId>
  <version>x.y.z</version>
</dependency>

<dependency>
  <groupId>no.bankaxept.epayment</groupId>
  <artifactId>wallet-client</artifactId>
  <version>x.y.z</version>
</dependency>
```
These artifact rely on an HTTP client loaded through Java's Service Provider Interface. There's one provided by thiskit
development kit, based on Spring WebFlux:
```xml
<dependency>
  <groupId>no.bankaxept.epayment</groupId>
  <artifactId>webflux-client</artifactId>
  <version>x.y.z</version>
</dependency>
```
The artifacts are available on a GitHub Packages repository, so make sure to include the following in your pom.xml as
well:
```xml
<repositories>
  <repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/BankAxept/bankaxept-epayment-development-kit</url>
  </repository>
</repositories>
```
_Note: GitHub Packages does not allow anonymous access for downloading Maven artifacts. A GitHub personal access
token (PAT) with `read:packages` scope will provide sufficient access._

## HTTP client

If you want to use your own HTTP client with the Java clients you can provide it through Java Service Provider Interface
by implementing the following interfaces:
* [HttpClient](java/base-client/src/main/java/no/bankaxept/epayment/client/base/http/HttpClient.java)
* [HttpClientProvider](java/base-client/src/main/java/no/bankaxept/epayment/client/base/spi/HttpClientProvider.java)

Then create a configuration file (like [this](java/webflux-client/src/main/resources/META-INF/services/no.bankaxept.epayment.client.base.spi.HttpClientProvider)). The [webflux-client module](java/webflux-client) can be used for reference.

## Java client usage

Each of the three Java clients can be constructed by providing the following parameters:
* authorizationServerUrl: URL for the OAuth2 authorization server.
* resourceServerUrl: URL for the Merchant API, Token Requestor API or Wallet API.
* clientId: Username for authenticating with the authorization server.
* clientSecret: Password for authenticating with the authorization server

Contact BankAxept for appropriate values.
