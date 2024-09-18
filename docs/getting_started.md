<p align="center">
<img alt="BankAxept_Logo.svg" src="../assets/images/bankaxept_logo.svg" width="300"/>
</p>


> Welcome to the Getting Started guide for the BankAxept 
> Epayment Platform (EPP). 
> 
> This text is intended to give both clear descriptions and 
> guidance for integrators and consumers of EPP. It includes an
> overview of all critical features as well as some general
> hints of how to utilize this platform.


# Introduction
EPaymentPlatform (EPP) is a Payment API for integrators (Integrator) utilizing BankAxept online payments.
It is based on a core principle of asynchronous exchange of information where the transactions created can be identified using an EPP defined `PaymentId` and an integrator defined `CorrelationId`.  
Requests also are idempotent on an Integrator defined `MessageId` as seen in our [MessageId section](#messageid).
Subsequent transaction operations (Capture, Refund etc.) can thereafter be performed as seen according to the [Payments Request](./swagger/integrator_merchant_bankaxept.md) component part our API spec.

## Setting up your EPP integration

In order to set up your EPP integration and start requesting payments, the following operations need to
be performed.

1. Through your BankAxept contact point you should retrieve your unique identifier (ClientId) as an integrator.
2. Provide a list of IPs that you will be operating from, enabling us to append them to our Allow List. Any additional IPs need to be transmitted to the BankAxept ePayment team contact before being utilized.
3. Provide a `callBackURL` which we will utilize as our address prefix for all callbacks.
4. Provide the certified Authentication Provider which you  will use to Authenticate payments.
5. Create a bCrypt based hash of a secret of your choice. We recommend reading up on [bCrypt](https://en.wikipedia.org/wiki/Bcrypt#) to understand the mechanisms involved.
6. Send the resulting IPs, CallbackUrl and bCrypt hash to your BankAxept ePayment team contact.
7. Receive EPP's Public Certificate for encryption of sensitive data.
8. Generate an access token as described in the [Authorization](#authorization) section
9. Utilize the access token to perform payments as described in the [Creating a Payment](./enrolment_and_payment.md#creating-a-payment) section

## Authorization

Once the set-up steps are performed you can then integrate with the [Client Authorization Service](./swagger/integrator_accesstoken_bankaxept.md).
The request should contain the secret used to generate the bCrypt based hash as well as your ClientId. This should be sent as a [Basic Authentication](https://en.wikipedia.org/wiki/Basic_access_authentication)
The resulting access token has a 1-hour lifetime. We recommend refreshing it 5 minutes before end of life. The resulting `access_token` can then be used to authorize
towards all other endpoints by putting it in the `Authorization` header as Bearer token.

### bCrypt guidelines

We recommend using Spring for up to date bCrypt generation.
For example the following command will result in a satisfactory bCrypt hash.

```
# brew tap spring-io/tap && brew install spring-boot
spring encodepassword -a bcrypt <secret> 
```

### Authentication provider and Wallet provider flow and interoperation

Please see our [Authentication Provider setup and guidelines](./authentication_interoperability.md)

## End to end setup of profile diagram

```mermaid
sequenceDiagram
    actor IntegratorRepresentative
    actor ePaymentRepresentative
    participant Integrator
    participant ePaymentPlatform

    Integrator ->> Integrator: Create bCrypt hash from self defined secret.
    Integrator ->> IntegratorRepresentative: Transmit resulting bCrypt hash.
    IntegratorRepresentative ->> ePaymentRepresentative: Transmit resulting bCrypt hash.

    IntegratorRepresentative ->> ePaymentRepresentative: Provide set of IPs, Authentication Provider and CallbackUrl.
    ePaymentPlatform ->> ePaymentPlatform: create ClientId identifying the Integrator.
    ePaymentRepresentative ->> ePaymentPlatform: Configure Allow List IPs, Authentication Provider, bCrypt hash and CallbackUrl.
    ePaymentRepresentative -->> IntegratorRepresentative: Send EPP public certificate, ISS value, <br/> Token Requestor Name and the AuthenticationProvider-ISS field <br/> and ClientId.

    Note over IntegratorRepresentative,ePaymentRepresentative: Manual setup completed.

    Integrator ->> ePaymentPlatform: Generate access token.
    ePaymentPlatform ->> ePaymentPlatform: Verify request.
    Note right of ePaymentPlatform: Matching ClientId and performing <br/> a bCrypt hash on the secret.
    ePaymentPlatform -->> Integrator: Return Access token.
```

### Checklist for information exchange

| Information item(s)      | Description                                                                                                                                                   |
|--------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ClientId                 | Generated by the ePayment Platform and returned to Integrator                                                                                                 |
| CallbackUrl              | Defined by Integrator, will be the prefix address all Callback requests are sent. Peruse our [Callback](#Asynchronous-retry-policy) section for more context. |
| ClientSecret             | A bCrypt secret that is **kept secret at the Integrator** and used to generate access tokens.                                                                 |
| ClientSecret bCrypt Hash | A bCrypt secret hash that is sent to the ePaymentPlatform and configured to the Integrators clientID                                                          |
| Authentication Provider  | Inform the EPP team which Authentication Provider you will be utilizing                                                                                       |
| ISS                      | Once the your profile is set up your will receive the Issuer ID corresponding to the your Integrator or Authentication Provider profile.                      |
| EPP public key           | Is sent by EPP during setup, needed to encrypt parts of requests.                                                                                             |
| Token Requestor Name     | Is sent by EPP during setup, needs to be part of enrolment requests.                                                                                          |

## Integration guidelines

This section contains general guidelines for integrating with the EPP.

### Correlation ID
All requests support an `X-Correlation-Id` header which can be used to correlate requests and responses. This is especially useful if you always ensure to set this header to a unique value for each request.
The `X-Correlation-Id` is returned in the corresponding callback, allowing you an additional mechanism to correlate the callback with the original request. It is **required** to use this header for enabling traceability and support.

### MessageId
The system acts idempotent on any `messageId`. It is **required** that you use a robust uniqueness mechanism (For example UUIDv4 or similar mechanism) to ensure that each request has a unique `messageId`.

#### MessageId uniqueness & Callbacks
EPP creates a UUID that is used as a `messageId` for each callback that is used to distinguish between different requests. This `messageId` is considered to be part of the message exchange between EEP and the Integrator.
This means that the EPP requires you to act idempotent on the `messageId` in the callback. This is to ensure that you do not perform the same operation multiple times.

### Authentication of callbacks

In order to authenticate the callbacks you receive from EPP, you should utilize Mutual TLS. This may be done by utilizing the public key provided by EPP during setup. 
The same public key will be utilized in the certificate used for sending the callback. 

### Asynchronous retry policy
Any Asynchronous Requests will be retried if the Response from the Integrator is anything other than `2xx` or `4xx`. In the case of a `4xx` the response will be interpreted as a final state and not retried.

Retries will be performed first after 10 seconds, and thereafter with an exponential backoff for 24 hours. After 24 hours the retry attempts will stop.

The backoff will extend additionally at a rate of `1.5^X` seconds where X is the number of retries until a max retry interval of `10 minutes` is reached.

### Timeouts and expected response times
These data points are intended to give the Integrator an idea of expected behavior to optimize based on their own use case.
We expect the Integrator to know their own applications and user behavior best and therefore do not give any strict guidelines on how to handle timeouts.

`
Please note that the following guidelines are based on the assumption that there are no network issues or other external factors that might affect the response time.
Nor does it estimate any additional network time outside of EPP.
`

1. For payments the expected resolution time is 50-300 ms.
2. For payments, if we have not reached a resolution within 6.6 seconds we will return a `AuthorisationFailed` callback. We will then perform a technical reversal immediately on the payment in question.
3. The outbound callback request is expected to leave EPP within 1000 ms of the synchronous request resolving.
4. EPP times out waiting for a callback response after 10 seconds.
5. An enrollment request times out within 1 hour. If the request has not been resolved within this time frame the request will be considered failed and a callback will be sent to the Integrator.
