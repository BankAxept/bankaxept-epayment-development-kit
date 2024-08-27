

# Enrolling a card

In order to perform a payment a card first needs to be enrolled and tokenized. First you must gather the necessary information to perform an enrollment. This data is then sent to the ePayment Platform for tokenization.
The account number and NIN is used to identify the card that is to be tokenized. The EPP will then asynchronously send a callback to the Integrator's Callback Server with the result of the enrollment, as well as any lifecyclce changes.

The resulting Payment Token are then used as a reference to the account in subsequent payment requests.
 

## Tokenization

Once you have requested a token you should expect the asynchronous call to your Callback Server. your Callback Server must comply with our
[Integration Specification](/assets/swagger/swagger_integrator_token_requestor_bankaxept/).

The tokenization request includes `tokenRequestorReference`. A reference set by the token requestor to uniquely identify an enrolment request. 
EPP uses this reference in all communication with the token requestor about the enrolment status. It is recommended that a unique value per enrolment is used. However, it is not a requirement.

## Token lifecycle

As updates occur with the underlying payment source EPP will send updates according to our [Token Requestor Callback API](/assets/swagger/swagger_integrator_token_requestor_callback/) specification.


| Lifecyle update(s) | Description                                                                            |
|--------------------|----------------------------------------------------------------------------------------|
| Suspensions        | An update notifying of a token suspension, should not be implemented as a final state. |
| Resumptions        | An update notifying of a token resumption, the token may be used again for payments.   |
| Deletions          | The token should be considered deleted and not used again.                             |
| Expiry updates     | An update extending the expiry of a token.                                             |

```mermaid
sequenceDiagram 
    participant Integrator
    participant ePaymentPlatform
    
    Integrator ->> ePaymentPlatform: Request enrollment
    activate ePaymentPlatform
    ePaymentPlatform-->>Integrator: 200 OK.
    deactivate ePaymentPlatform
    
    ePaymentPlatform->> ePaymentPlatform: Resolve Enrollment Status
    
    ePaymentPlatform->>Integrator: Asynchronous Enrollment result callback
    activate Integrator
    Integrator-->>ePaymentPlatform: 200 OK.
    deactivate Integrator
    note left of Integrator: The callback contains a token <br/> which is used in subsequent Payment Requests.
    
    alt subsequent lifecycle updates (Suspend, Resume, Delete, Expiry updates)
    
    ePaymentPlatform->>Integrator: Asynchronous Token lifecycle update callback
    activate Integrator
    Integrator-->>ePaymentPlatform: 200 OK.
    deactivate Integrator


    note left of Integrator: Note that subsequent updates are initiated <br/> by the EPP and not the Integrator.


    end

```

# Creating a payment
A full overview of all available fields for a payment can be found in the [Payments Request](https://github.com/BankAxept/bankaxept-epayment-development-kit/blob/main/openapi/integrator/merchant/bankaxept.yaml) component part our API spec.

## Standard flow

Below is the Payment request's happy flow. Note that the PaymentId which
subsequent operations are performed with is contained in the asynchronous callback.

```mermaid
sequenceDiagram
    participant Integrator
    participant ePaymentPlatform
    
    Integrator->>ePaymentPlatform: RequestPayment
    activate ePaymentPlatform
    ePaymentPlatform-->>Integrator: 200 OK.
    deactivate ePaymentPlatform
    
    ePaymentPlatform ->> ePaymentPlatform : Resolve Payment Status

    ePaymentPlatform->>Integrator: Asynchronous Payment result callback!
    activate Integrator
    Integrator-->>ePaymentPlatform: 200 OK.
    deactivate Integrator
    note left of Integrator: The callback contains a paymentId <br/> which subsequent Transaction Operations are <br/> performed with.

    alt Subsequent operations (Capture, Refund, Cancel)
    
    Integrator ->> ePaymentPlatform: Transaction Operations.
    activate ePaymentPlatform
    ePaymentPlatform-->>Integrator: 200 OK
    deactivate ePaymentPlatform

    ePaymentPlatform ->> ePaymentPlatform : Resolve Payment Operation

    ePaymentPlatform->>Integrator: Asynchronous Payment Operation result callback!
    activate Integrator
    Integrator-->>ePaymentPlatform: 200 OK.
    deactivate Integrator
    
    end 
```

## Creating a payment guidelines.

``messageId``: The ``messageId``field is considered the Integrator's unique identifier of a PaymentRequest, and can be used in the case of a [Rollback Request](https://github.com/BankAxept/bankaxept-epayment-development-kit/blob/main/openapi/integrator/merchant/bankaxept.yaml)
of an ongoing payment. The ePaymentPlatform performs duplicate controls on the ``messageId`` field, and acts idempotent on requests with the same ``messageId``. Therefore, it *must* be unique pr separate payment request. Meaning that if multiple are done for the same Order (for example a retry due to a previously failed payment request.), a new ``messageId`` must be used.

``merchantOrderReference``: The ``merchantOrderReference``field is considered a reference to the Merchant's Order which might be distinct from the Integrator's own ``messageId``.

## Callbacks

### Payment Callbacks
The required Callback API for an ePayment Platform Payment may be found [in the Partner Yaml](https://github.com/BankAxept/bankaxept-epayment-development-kit/blob/main/openapi/integrator/merchant/partner.yaml) definition.