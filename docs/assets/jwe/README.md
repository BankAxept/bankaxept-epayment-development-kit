# JWE Test Vector

This directory contains intentionally public, nonproduction RSA key material for validating compact JSON Web Encryption
examples in the BankAxept ePayment API specifications.

## Authentication Provider JWS Vectors

`certs/authentication-provider-key.pem` and `certs/authentication-provider-cert.pem` are an intentionally public,
nonproduction 2048 bit RSA Authentication Provider signing key pair. The `verifiedCardholderAuthenticationSignedData`
example in [the shared API components](../../../openapi/integrator/components.yaml) uses PS256 and contains this
Payment PermissionGrant payload.

### Payment

```json
{"type":"payment.v1","iat":1750000000,"iss":"authentication-provider-id","nonce":"550e8400-e29b-41d4-a716-446655440000","sub":"nnin:13087512345","permissionId":"payment-permission-id","digest":"QomjM9YUvFcj0bd0Xjr39uMTaKzb1D54H_YAbHicy4Q"}
```

### Account Number Enrolment

```json
{"type":"approveAccount.v1","iat":1750000000,"iss":"authentication-provider-id","nonce":"a05b53be-718e-4df2-80ac-83696b711111","sub":"nnin:13087512345","permissionId":"enrolment-permission-id","digest":"-PAwASn3A47p15X48mrqBL-pWdcKtktj8bqTYCW0yn4"}
```

## EPP Recipient Vectors

`certs/epp-key.pem` and `certs/epp-cert.pem` decrypt and encrypt the
`encryptedEnrolmentCardholderAuthenticationData` example in
[the Token Requestor API specification](../../../openapi/integrator/token-requestor/bankaxept.yaml). This vector
represents an inbound account number enrolment authentication request, including the
`approveAccount.v1` PermissionGrant in `verifiedCardholderAuthenticationSignedData`.

## Wallet Recipient Vector

`certs/wallet-key.pem` and `certs/wallet-cert.pem` decrypt and encrypt the `encryptedAccountNumber` callback example in
[the Token Requestor API specification](../../../openapi/integrator/token-requestor/partner.yaml). Its plaintext is
`99980000008`.

EPP uses the Wallet public certificate to encrypt the `encryptedAccountNumber` callback field.

## Payment Authentication Vector

The EPP key pair decrypts and encrypts the `encryptedPaymentCardholderAuthenticationData` example in
[the shared API components](../../../openapi/integrator/components.yaml). This vector includes the `payment` signed
JWS.

## Security Warning

Do not use the Authentication Provider, EPP, or Wallet key pair for encryption, signing, or any production purpose.

## Algorithms

The compact JWE uses `RSA-OAEP-256` for key management and `A256CBC-HS512` for content encryption.
