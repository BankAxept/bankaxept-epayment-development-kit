# JWE Test Vector

This directory contains intentionally public, nonproduction RSA key material for validating compact JSON Web Encryption
examples in the BankAxept ePayment API specifications.

## EPP Recipient Vector

`certs/epp-key.pem` and `certs/epp-cert.pem` decrypt and encrypt the
`encryptedEnrolmentCardholderAuthenticationData` example in
[the Token Requestor API specification](../../../openapi/integrator/token-requestor/bankaxept.yaml). This vector
represents an inbound account number enrolment authentication request. Its decrypted JSON is:

```json
{"iss":"token-requestor-id","iat":1750000000,"enrolmentData":{"nin":"13087512345","accountNumber":"99980000008"}}
```

## Wallet Recipient Vector

`certs/wallet-key.pem` and `certs/wallet-cert.pem` decrypt and encrypt the `encryptedAccountNumber` callback example in
[the Token Requestor API specification](../../../openapi/integrator/token-requestor/partner.yaml). Its plaintext is
`99980000008`.

EPP uses the Wallet public certificate to encrypt the `encryptedAccountNumber` callback field.

## Security Warning

Do not use either EPP or Wallet key pair for encryption or any production purpose.

## Algorithms

The compact JWE uses `RSA-OAEP-256` for key management and `A256CBC-HS512` for content encryption.
