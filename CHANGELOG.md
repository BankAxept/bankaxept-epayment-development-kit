# Changelog

## [2.2.0](https://github.com/BankAxept/bankaxept-epayment-development-kit/compare/v2.1.3...v2.2.0) (2025-03-11)


### Features

* Add netSettlementAmount to the settlement callback response ([#233](https://github.com/BankAxept/bankaxept-epayment-development-kit/issues/233)) ([86270a0](https://github.com/BankAxept/bankaxept-epayment-development-kit/commit/86270a0557a5bf723c816bfc07dcc991e07a5514))

## [2.1.3](https://github.com/BankAxept/bankaxept-epayment-development-kit/compare/v2.1.2...v2.1.3) (2025-03-05)


### Bug Fixes

* change networkReference maxLength to 36 ([#230](https://github.com/BankAxept/bankaxept-epayment-development-kit/issues/230)) ([2201449](https://github.com/BankAxept/bankaxept-epayment-development-kit/commit/22014494a66c59da93c75c1e05f174f5e44fdedf))

## [2.1.2](https://github.com/BankAxept/bankaxept-epayment-development-kit/compare/v2.1.1...v2.1.2) (2025-03-05)


### Bug Fixes

* Add networkReference to PaymentRequest, CaptureRequest and RefundRequest ([#227](https://github.com/BankAxept/bankaxept-epayment-development-kit/issues/227)) ([7f3bc6f](https://github.com/BankAxept/bankaxept-epayment-development-kit/commit/7f3bc6f3c58b4523b3085e6f2e9d8318de7fc113))

## [2.1.1](https://github.com/BankAxept/bankaxept-epayment-development-kit/compare/v2.1.0...v2.1.1) (2025-02-28)


### Bug Fixes

* Add error codes for refund declines ([#219](https://github.com/BankAxept/bankaxept-epayment-development-kit/issues/219)) ([532111a](https://github.com/BankAxept/bankaxept-epayment-development-kit/commit/532111a79e02b89fe3eda9a5566a7ec9d8af3416))
* Rename request field in card eligibility endpoint ([#222](https://github.com/BankAxept/bankaxept-epayment-development-kit/issues/222)) ([766b519](https://github.com/BankAxept/bankaxept-epayment-development-kit/commit/766b51930e681c2470233ee3a6e573faf01569fc))


### Dependency Updates

* bump io.swagger.parser.v3:swagger-parser from 2.1.23 to 2.1.25 ([#203](https://github.com/BankAxept/bankaxept-epayment-development-kit/issues/203)) ([fe69efd](https://github.com/BankAxept/bankaxept-epayment-development-kit/commit/fe69efd31dfbb09f8ab80ec03316929162936f12))
* bump org.springframework.boot:spring-boot-starter-parent ([#218](https://github.com/BankAxept/bankaxept-epayment-development-kit/issues/218)) ([ddc07b2](https://github.com/BankAxept/bankaxept-epayment-development-kit/commit/ddc07b2b67e8e72c2a51bf53ab05d8a5ebbe930f))

## [2.1.0](https://github.com/BankAxept/bankaxept-epayment-development-kit/compare/v2.0.2...v2.1.0) (2025-02-21)


### Features

* Add card eligibility endpoint ([#208](https://github.com/BankAxept/bankaxept-epayment-development-kit/issues/208)) ([28591cc](https://github.com/BankAxept/bankaxept-epayment-development-kit/commit/28591cc15e40ed966a40b716023da6bc43d60d0d))

## [2.0.2](https://github.com/BankAxept/bankaxept-epayment-development-kit/compare/v2.0.1...v2.0.2) (2025-02-17)


### Bug Fixes

* Add merchant cancellation callback API ([#212](https://github.com/BankAxept/bankaxept-epayment-development-kit/issues/212)) ([0a38aed](https://github.com/BankAxept/bankaxept-epayment-development-kit/commit/0a38aeda97c6bdef4f9a2ef544127ad216fc2471))

## [2.0.1](https://github.com/BankAxept/bankaxept-epayment-development-kit/compare/v2.0.0...v2.0.1) (2025-02-10)


### Bug Fixes

* Add error codes for Refund and Capture operations ([#202](https://github.com/BankAxept/bankaxept-epayment-development-kit/issues/202)) ([1b501e9](https://github.com/BankAxept/bankaxept-epayment-development-kit/commit/1b501e986704c10b023c8d5c709657c6900944d8))

## [2.0.0](https://github.com/BankAxept/bankaxept-epayment-development-kit/compare/v1.2.1...v2.0.0) (2024-11-01)


### ⚠ BREAKING CHANGES

* Add status and current batch number to cut off response ([#178](https://github.com/BankAxept/bankaxept-epayment-development-kit/issues/178))

### Features

* Add status and current batch number to cut off response ([#178](https://github.com/BankAxept/bankaxept-epayment-development-kit/issues/178)) ([7f7f6ae](https://github.com/BankAxept/bankaxept-epayment-development-kit/commit/7f7f6aed011c595d29c0553b7efe9763f2c93fcc))


### Dependency Updates

* bump io.swagger.parser.v3:swagger-parser from 2.1.22 to 2.1.23 ([#187](https://github.com/BankAxept/bankaxept-epayment-development-kit/issues/187)) ([e11e01b](https://github.com/BankAxept/bankaxept-epayment-development-kit/commit/e11e01bfe5cba4fca8e8cbd3c339bf241e892484))

## [1.2.1](https://github.com/BankAxept/bankaxept-epayment-development-kit/compare/v1.2.0...v1.2.1) (2024-10-16)


### Bug Fixes

* Add ErrorCode as a property to CardEnrolmentResponse ([#184](https://github.com/BankAxept/bankaxept-epayment-development-kit/issues/184)) ([3780ad9](https://github.com/BankAxept/bankaxept-epayment-development-kit/commit/3780ad975eab24632a25da9374074a2ee8b3f00b))

## [1.2.0](https://github.com/BankAxept/bankaxept-epayment-development-kit/compare/v1.1.1...v1.2.0) (2024-10-14)


### Features

* Remove enrolment statuses and add errocodes by aligning to new structure ([#180](https://github.com/BankAxept/bankaxept-epayment-development-kit/issues/180)) ([1ec7955](https://github.com/BankAxept/bankaxept-epayment-development-kit/commit/1ec7955a9ba2578cece0cbb66609c63e88ca17a5))

## [1.1.1](https://github.com/BankAxept/bankaxept-epayment-development-kit/compare/v1.1.0...v1.1.1) (2024-09-05)


### Bug Fixes

* Fix duplicate enrolment status reason ([#169](https://github.com/BankAxept/bankaxept-epayment-development-kit/issues/169)) ([6442763](https://github.com/BankAxept/bankaxept-epayment-development-kit/commit/6442763f478b5f53411b3c8ece43f604917ac881))

## [1.1.0](https://github.com/BankAxept/bankaxept-epayment-development-kit/compare/1.0.3...v1.1.0) (2024-09-05)


### Features

* Add new enrolment status to api specification ([#166](https://github.com/BankAxept/bankaxept-epayment-development-kit/issues/166)) ([762b380](https://github.com/BankAxept/bankaxept-epayment-development-kit/commit/762b380eed03fb9f08665cf554d7c94c98e464ef))


### Bug Fixes

* Bump org.openapitools:openapi-generator-maven-plugin from 7.5.0 to 7.6.0 ([#144](https://github.com/BankAxept/bankaxept-epayment-development-kit/issues/144)) ([5749561](https://github.com/BankAxept/bankaxept-epayment-development-kit/commit/5749561064bf6694904958c8c304dc7be0daace6))
