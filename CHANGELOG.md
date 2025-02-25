# [1.0.0](https://github.com/hyperledger/identus-mediator/compare/v0.15.0...v1.0.0) (2024-10-02)


### Bug Fixes

* add env vars for SERVICE_ENDPOINTS ([#335](https://github.com/hyperledger/identus-mediator/issues/335)) ([f2b078a](https://github.com/hyperledger/identus-mediator/commit/f2b078aa3b58fddc7f85af9e1d46503680263761))
* rename repo link to hyperledger/identus-mediator ([#331](https://github.com/hyperledger/identus-mediator/issues/331)) ([69f0124](https://github.com/hyperledger/identus-mediator/commit/69f012453fe29d7c1da6846b4137f187dfe4e8ac))
* wrong mediator repository in docker compose ([#334](https://github.com/hyperledger/identus-mediator/issues/334)) ([a43fe38](https://github.com/hyperledger/identus-mediator/commit/a43fe38ddbcf5d75fcade7f62d416ef3f752cf0a))


### chore

* Mediator Version 1.0.0 ([#354](https://github.com/hyperledger/identus-mediator/issues/354)) ([af65517](https://github.com/hyperledger/identus-mediator/commit/af65517c1337e540d0a92d1f758c8ce9fe3ebe67))


### BREAKING CHANGES

* Mediator Version 1.0.0

Signed-off-by: FabioPinheiro <fabiomgpinheiro@gmail.com>

# 0.15.0 (2024-07-10)


### Bug Fixes

* Add and enable websocket on apisix route ([#179](https://github.com/hyperledger/identus-mediator/issues/179)) ([9060b73](https://github.com/hyperledger/identus-mediator/commit/9060b73f953436152407069431dc4519400e67e5))
* Added DidSubject to report problem when not enrolled ([#111](https://github.com/hyperledger/identus-mediator/issues/111)) ([397a00a](https://github.com/hyperledger/identus-mediator/commit/397a00aa10589b68a062f3b664617af19c3e30c2))
* alias list is empty in new accounts by default ([#87](https://github.com/hyperledger/identus-mediator/issues/87)) ([c35ba20](https://github.com/hyperledger/identus-mediator/commit/c35ba20195fadf260b9c0c2cbad3d03aed7305fd))
* allowed memory to increase gradually in CI ([#37](https://github.com/hyperledger/identus-mediator/issues/37)) ([59eb4e6](https://github.com/hyperledger/identus-mediator/commit/59eb4e6d5eb41e85d33bddb6298ae0f17acbd196))
* Annotate Headers & Update zio-http to version 3.0.0-RC2 ([#106](https://github.com/hyperledger/identus-mediator/issues/106)) ([37e3343](https://github.com/hyperledger/identus-mediator/commit/37e33435cbdd5cac4d751a6cf814232f7947fa5b))
* Change atala-prism-mediator to identus-mediator ([#287](https://github.com/hyperledger/identus-mediator/issues/287)) ([0341a45](https://github.com/hyperledger/identus-mediator/commit/0341a45f96f0c975177d43651d43b062a9aab824))
* Change from name var to fix name ([#286](https://github.com/hyperledger/identus-mediator/issues/286)) ([c6d046f](https://github.com/hyperledger/identus-mediator/commit/c6d046fafc3372f7557315f4d69503be9050d64f))
* cleanup IOHK reference ([#325](https://github.com/hyperledger/identus-mediator/issues/325)) ([cd8dee9](https://github.com/hyperledger/identus-mediator/commit/cd8dee9b97cd9dfc5585768809a090ae000a82a8))
* collectionName messages.outbound ([#92](https://github.com/hyperledger/identus-mediator/issues/92)) ([fa09262](https://github.com/hyperledger/identus-mediator/commit/fa0926256acc1d5aa9fa6c3e71987fa35af7a1b1))
* correct endpoint for wss in deployment.yaml ([#178](https://github.com/hyperledger/identus-mediator/issues/178)) ([b56b06c](https://github.com/hyperledger/identus-mediator/commit/b56b06c3825b45a0d5ba50bcf40cf63f0440c713))
* disabled documentation generation ([#24](https://github.com/hyperledger/identus-mediator/issues/24)) ([6fe228f](https://github.com/hyperledger/identus-mediator/commit/6fe228f316d8c03a36cb35f4b11cfb302edc2912))
* Discord links to never expires ([#141](https://github.com/hyperledger/identus-mediator/issues/141)) ([49ce87c](https://github.com/hyperledger/identus-mediator/commit/49ce87c65fb1ba99e5ba2905dae9609f2389a049))
* dynamic DID on the web page ([#40](https://github.com/hyperledger/identus-mediator/issues/40)) ([dcf6f58](https://github.com/hyperledger/identus-mediator/commit/dcf6f58f759a7997be1680d706e6e00a70f6a455))
* Encoder for VerificationMethodReferenced as String ([#110](https://github.com/hyperledger/identus-mediator/issues/110)) ([fda478f](https://github.com/hyperledger/identus-mediator/commit/fda478fc962cd47137095efdcd7dd6868512f337))
* execute the ProtocolExecute's jobToRun zio ([#8](https://github.com/hyperledger/identus-mediator/issues/8)) ([e8eb809](https://github.com/hyperledger/identus-mediator/commit/e8eb8098b29ccfb1268b65129604c77457377d17))
* field name routing_did in keylist response body ([#22](https://github.com/hyperledger/identus-mediator/issues/22)) ([eace0fd](https://github.com/hyperledger/identus-mediator/commit/eace0fd85e863f39cff9e5ce08de425df2e9b3f9))
* Fix release job (add more ram) ([#32](https://github.com/hyperledger/identus-mediator/issues/32)) ([39d5719](https://github.com/hyperledger/identus-mediator/commit/39d5719dcbf1443e50c25d935b00e7e66a89fed4))
* force release.. ([#190](https://github.com/hyperledger/identus-mediator/issues/190)) ([96baea3](https://github.com/hyperledger/identus-mediator/commit/96baea3f09192e88f80abeac8d96517e8e6eb542))
* Hardcode the atala prism did identity ([#9](https://github.com/hyperledger/identus-mediator/issues/9)) ([2be3df0](https://github.com/hyperledger/identus-mediator/commit/2be3df07092b01ab34421fa318959d4ee9cfc95d))
* Id/hash of the message must be deterministic ([#53](https://github.com/hyperledger/identus-mediator/issues/53)) ([4ec7396](https://github.com/hyperledger/identus-mediator/commit/4ec73966ea70100f60db0b6c73534c1af75b8d63))
* incorrect body of the message results in problem report ([#161](https://github.com/hyperledger/identus-mediator/issues/161)) ([f866daa](https://github.com/hyperledger/identus-mediator/commit/f866daa8960c713da7b053eb0f8378a3bab01080))
* **infra:** add sync-wave to certificate template ([#134](https://github.com/hyperledger/identus-mediator/issues/134)) ([daecc08](https://github.com/hyperledger/identus-mediator/commit/daecc08e897830beb9bb0a6329c13afcf5588c9b))
* make content-type header case insensitive & remove support for ws ([#7](https://github.com/hyperledger/identus-mediator/issues/7)) ([c2f064e](https://github.com/hyperledger/identus-mediator/commit/c2f064e8614d0a8085975a64f0246c11abff33e7))
* mediator ATL-4883 pickup status message to sync reply ([#59](https://github.com/hyperledger/identus-mediator/issues/59)) ([eb5ce56](https://github.com/hyperledger/identus-mediator/commit/eb5ce563b5363be3e4fb718a5fda927b2c6cc0c5))
* mediator db not storing the original value for protected header ([#15](https://github.com/hyperledger/identus-mediator/issues/15)) ([1bba75f](https://github.com/hyperledger/identus-mediator/commit/1bba75f26b2e4c630bc2cc7b399c731705805abf))
* mediator docker-compose image repo and version ([#39](https://github.com/hyperledger/identus-mediator/issues/39)) ([803b74c](https://github.com/hyperledger/identus-mediator/commit/803b74c037aad864111808fe38250e7e6586b5e4))
* mediator oob webpage added logo  ([#42](https://github.com/hyperledger/identus-mediator/issues/42)) ([faf43d9](https://github.com/hyperledger/identus-mediator/commit/faf43d9eccdada8a17702cec995d76b5bbcffa68))
* mediator rename package and refactoring ([#41](https://github.com/hyperledger/identus-mediator/issues/41)) ([286e742](https://github.com/hyperledger/identus-mediator/commit/286e7420127aa67b5bc82c98dca2b93b09053067))
* mediator test ([#71](https://github.com/hyperledger/identus-mediator/issues/71)) ([c32f34e](https://github.com/hyperledger/identus-mediator/commit/c32f34e517abda279d51aae75d278c74921ddc52))
* mediator unique constraint issue ([#25](https://github.com/hyperledger/identus-mediator/issues/25)) ([c1f0230](https://github.com/hyperledger/identus-mediator/commit/c1f0230a35f771b9d1b13530a1784a8aee9b40a1))
* **mediator:** Enabled X-Request-Id ATL-5568 ([#104](https://github.com/hyperledger/identus-mediator/issues/104)) ([f96d683](https://github.com/hyperledger/identus-mediator/commit/f96d6834456847c9b4a8dd65583fcac7e1548a13))
* **mediator:** Update the readme and docker with type of key forma… ([#267](https://github.com/hyperledger/identus-mediator/issues/267)) ([529be58](https://github.com/hyperledger/identus-mediator/commit/529be58e95b48f9133fc9f25c64fcff7c67064ee))
* MissingProtocolExecuter ([#140](https://github.com/hyperledger/identus-mediator/issues/140)) ([d71bca5](https://github.com/hyperledger/identus-mediator/commit/d71bca590ebfc79e707fef7621e7b14aaa3c5b4a))
* mongodb init script updated infrastructure mongodb yaml ([#94](https://github.com/hyperledger/identus-mediator/issues/94)) ([a900d64](https://github.com/hyperledger/identus-mediator/commit/a900d6489b069d9ca9f08c31f39c6d723002cf6c))
* OutOfBand qrcode with base url ([#133](https://github.com/hyperledger/identus-mediator/issues/133)) ([28df290](https://github.com/hyperledger/identus-mediator/commit/28df2904000958c553f0ede66f7e4de06af7a8d1))
* parsing error when return_route is none ([#77](https://github.com/hyperledger/identus-mediator/issues/77)) ([672037d](https://github.com/hyperledger/identus-mediator/commit/672037de9dd97fddf94b5eebc41eb23fafbeb89c))
* re-enable the logging based on X-Request-Id header ([#211](https://github.com/hyperledger/identus-mediator/issues/211)) ([0136326](https://github.com/hyperledger/identus-mediator/commit/01363261a2100f5ff0650b94af14d733c09cc400))
* register the transport ([#192](https://github.com/hyperledger/identus-mediator/issues/192)) ([76d4b35](https://github.com/hyperledger/identus-mediator/commit/76d4b358023b96906f975eaaa81dd6d7bf5d28ae))
* Release mediator tagFormat ([#285](https://github.com/hyperledger/identus-mediator/issues/285)) ([602f0e5](https://github.com/hyperledger/identus-mediator/commit/602f0e52d050f02aafed0ec4b3ac491b6035f3e5))
* reply condition check ReturnRoute ([#88](https://github.com/hyperledger/identus-mediator/issues/88)) ([c2a091c](https://github.com/hyperledger/identus-mediator/commit/c2a091c4dd17f9807c16a2eb446de7970e9a2e4a))
* sbt config enable docker plugin ([#6](https://github.com/hyperledger/identus-mediator/issues/6)) ([4cf6d90](https://github.com/hyperledger/identus-mediator/commit/4cf6d901a4a7c6f2be5523eb1eff0cf2db340506))
* Send problemReport for duplicate message ([#157](https://github.com/hyperledger/identus-mediator/issues/157)) ([bf589df](https://github.com/hyperledger/identus-mediator/commit/bf589df043f5dc6a71714045530903724e99c0b4))
* send status message on delivery request If no messages are avail… ([#139](https://github.com/hyperledger/identus-mediator/issues/139)) ([f42239d](https://github.com/hyperledger/identus-mediator/commit/f42239d504c181c8f00b96fb2a3373071f1d8766))
* set default endpoint to localhost ([#102](https://github.com/hyperledger/identus-mediator/issues/102)) ([b607ca2](https://github.com/hyperledger/identus-mediator/commit/b607ca2d5b11cb12209c93282ee500b3eba5e12a))
* sign ProblemReport when TO is unspecified ([#105](https://github.com/hyperledger/identus-mediator/issues/105)) ([670d5e4](https://github.com/hyperledger/identus-mediator/commit/670d5e42af40806ea832edc2e02b971e104436e7))
* Support alias (DID) in Live mode ([#235](https://github.com/hyperledger/identus-mediator/issues/235)) ([5d258bb](https://github.com/hyperledger/identus-mediator/commit/5d258bb1efb180bfa5b0f2e937b3c7be0267795b)), closes [#230](https://github.com/hyperledger/identus-mediator/issues/230)
* trustPing call back not working ([#23](https://github.com/hyperledger/identus-mediator/issues/23)) ([303af83](https://github.com/hyperledger/identus-mediator/commit/303af830e81b470b1abfe0148499c5f924c26013))
* Update local docker-compose configuration by adding the SERVICE_ENDPOINT port ([#128](https://github.com/hyperledger/identus-mediator/issues/128)) ([071f929](https://github.com/hyperledger/identus-mediator/commit/071f929c4a811afb90bbcffde47db6fd0f95bc8d))
* Update README.md ([#292](https://github.com/hyperledger/identus-mediator/issues/292)) ([9f3e3d9](https://github.com/hyperledger/identus-mediator/commit/9f3e3d990f63fd5a160a71730432714cc7fe9e55))
* UserAccountRepo.createOrFindDidAccount ([#69](https://github.com/hyperledger/identus-mediator/issues/69)) ([dc2ccfd](https://github.com/hyperledger/identus-mediator/commit/dc2ccfd80cc041f4acaf2484466cd541c35a47d2))
* websocket correct indentation ([#182](https://github.com/hyperledger/identus-mediator/issues/182)) ([4a5b607](https://github.com/hyperledger/identus-mediator/commit/4a5b60780b9f608dc2e9137d4b3d80bf826cc83f))


### Features

* Add config from application.conf and docker-compose and an example ([#10](https://github.com/hyperledger/identus-mediator/issues/10)) ([0031067](https://github.com/hyperledger/identus-mediator/commit/0031067c19aeb03457048bc34a4e5cd9e35230d8))
* Add docker config to build.sbt ([#5](https://github.com/hyperledger/identus-mediator/issues/5)) ([b2908ad](https://github.com/hyperledger/identus-mediator/commit/b2908ade8713e021460d488c9c0f731c8a355954))
* add endpoint to get the OOB mediate invitation ([#63](https://github.com/hyperledger/identus-mediator/issues/63)) ([11ef6d1](https://github.com/hyperledger/identus-mediator/commit/11ef6d1d0a7a3034a67a4cfcb5b979b55066075d))
* add helm-chart for mediator ([#64](https://github.com/hyperledger/identus-mediator/issues/64)) ([c379a62](https://github.com/hyperledger/identus-mediator/commit/c379a62d6d7c63369a8bb3974043642873f9e795)), closes [#61](https://github.com/hyperledger/identus-mediator/issues/61) [#63](https://github.com/hyperledger/identus-mediator/issues/63)
* add MediatorBuildInfo & /did and /version endpoints ([#120](https://github.com/hyperledger/identus-mediator/issues/120)) ([e41098a](https://github.com/hyperledger/identus-mediator/commit/e41098a1dddffe91df0c70c388a2bd77ea960081))
* Add Storage ([#8](https://github.com/hyperledger/identus-mediator/issues/8)) ([ebf4597](https://github.com/hyperledger/identus-mediator/commit/ebf45971ee7ae5d3c9e149469253f9ce4f7299dc))
* add tag latest to the mediator docker image ([#118](https://github.com/hyperledger/identus-mediator/issues/118)) ([1ae2b87](https://github.com/hyperledger/identus-mediator/commit/1ae2b87805e9879bc6648f159277e579c46eaa93))
* Added test for executor and problem reports  ([#117](https://github.com/hyperledger/identus-mediator/issues/117)) ([8e26535](https://github.com/hyperledger/identus-mediator/commit/8e26535df4bdd1dcbd03cd7b798574efc62cccc4))
* Better error handling for connection refused ([#47](https://github.com/hyperledger/identus-mediator/issues/47)) ([80d1956](https://github.com/hyperledger/identus-mediator/commit/80d1956ef13b5f1e42c810eb9b4dee86fa497cb9))
* Error handling and Send Problem Reports ([#65](https://github.com/hyperledger/identus-mediator/issues/65)) ([933cefc](https://github.com/hyperledger/identus-mediator/commit/933cefc56f1cc066dd434d97b29ec11faef6ad44))
* fix mongo config when no port & support gzip ([#35](https://github.com/hyperledger/identus-mediator/issues/35)) ([11c9330](https://github.com/hyperledger/identus-mediator/commit/11c9330c62941a67405e51c2d243c0d21078b4d9))
* Helm Chart Publish ([#301](https://github.com/hyperledger/identus-mediator/issues/301)) ([9f0e118](https://github.com/hyperledger/identus-mediator/commit/9f0e11848cbc630669b1b82c042a07117d8422e8))
* helm-chart appVersion bump and addtional fixes ([#67](https://github.com/hyperledger/identus-mediator/issues/67)) ([539150b](https://github.com/hyperledger/identus-mediator/commit/539150be61bc228956d1e7c808bed9b1e7527c8e))
* mediator  added more logs and associated message Hash and structured logging ([#16](https://github.com/hyperledger/identus-mediator/issues/16)) ([d9c6926](https://github.com/hyperledger/identus-mediator/commit/d9c6926c0232a98d094434ed00848eaa8dcbdaf5))
* mediator added test for storage layer ([#13](https://github.com/hyperledger/identus-mediator/issues/13)) ([969f50c](https://github.com/hyperledger/identus-mediator/commit/969f50c86d1255972414c48024891897991a97be))
* mediator initial release ([#4](https://github.com/hyperledger/identus-mediator/issues/4)) ([98ee905](https://github.com/hyperledger/identus-mediator/commit/98ee9056b6506746b96fa6a209d97b6ece7e13eb))
* mediator rename package and organise the packages ([#12](https://github.com/hyperledger/identus-mediator/issues/12)) ([594081c](https://github.com/hyperledger/identus-mediator/commit/594081cddd96431317412ccdc8969c461dc9e9d7))
* new a Webapp to show the QR Code  ([#21](https://github.com/hyperledger/identus-mediator/issues/21)) ([d47fc0d](https://github.com/hyperledger/identus-mediator/commit/d47fc0da52426e08030acf26509d7c7a99d1b356))
* Not send response errors to the caller ([#50](https://github.com/hyperledger/identus-mediator/issues/50)) ([6cf0ac1](https://github.com/hyperledger/identus-mediator/commit/6cf0ac1f9fbbc57778732157be0433f1b8d6604b))
* process KeylistQuery and return Keylist ([#30](https://github.com/hyperledger/identus-mediator/issues/30)) ([04d2980](https://github.com/hyperledger/identus-mediator/commit/04d2980e7e7d170a1bda64fa3eab4794d2ff750c))
* reply asynchronous unless return_route all ([#86](https://github.com/hyperledger/identus-mediator/issues/86)) ([a306d59](https://github.com/hyperledger/identus-mediator/commit/a306d5979be83cbca3f054d5c10408adf6d22b69))
* reply to StatusRequest ([#26](https://github.com/hyperledger/identus-mediator/issues/26)) ([3b44d2f](https://github.com/hyperledger/identus-mediator/commit/3b44d2f87985deb55c296dc7d3fda05b16a36e17))
* Return 202 HTTP code (for DID Comm) ([#162](https://github.com/hyperledger/identus-mediator/issues/162)) ([2ceab73](https://github.com/hyperledger/identus-mediator/commit/2ceab7333c23edab0b7b3ca5656aecdd04213082)), closes [#160](https://github.com/hyperledger/identus-mediator/issues/160)
* Store outbound messages ([#84](https://github.com/hyperledger/identus-mediator/issues/84)) ([e97cd1d](https://github.com/hyperledger/identus-mediator/commit/e97cd1d2bfba95e17afb74af2bf27ff0be217cc2))
* Support protocol discover-features 2.0 ([#154](https://github.com/hyperledger/identus-mediator/issues/154)) ([0e93dca](https://github.com/hyperledger/identus-mediator/commit/0e93dcaf881e5e990791306f61e34244bde5fa62))
* Update ScalaDID to 0.1.0-M18 and support new format of DID Peer ([#204](https://github.com/hyperledger/identus-mediator/issues/204)) ([551ce49](https://github.com/hyperledger/identus-mediator/commit/551ce4901707c0075d6cfcf896641727868a3da3)), closes [#158](https://github.com/hyperledger/identus-mediator/issues/158)
* Update timeout settings for websocket ([#191](https://github.com/hyperledger/identus-mediator/issues/191)) ([bfd228f](https://github.com/hyperledger/identus-mediator/commit/bfd228fa77d2ae0f76ed091e98ebd42849474e11))
* websockets support ([#172](https://github.com/hyperledger/identus-mediator/issues/172)) ([3d32b51](https://github.com/hyperledger/identus-mediator/commit/3d32b51bd357904c8ab19c53b98ac1c22bbb4b82))

# [0.15.0-beta.2](https://github.com/hyperledger/identus-mediator/compare/v0.15.0-beta.1...v0.15.0-beta.2) (2024-07-10)


### Bug Fixes

* Hyperledger Bot DCO ([0d9ca67](https://github.com/hyperledger/identus-mediator/commit/0d9ca67950dedca5c6f4389b17a70fd496073df3))

## [0.14.2](https://github.com/input-output-hk/atala-prism-mediator/compare/prism-mediator-v0.14.1...prism-mediator-v0.14.2) (2024-04-04)


### Bug Fixes

* **mediator:** Update the readme and docker with type of key forma… ([#267](https://github.com/input-output-hk/atala-prism-mediator/issues/267)) ([236620f](https://github.com/input-output-hk/atala-prism-mediator/commit/236620f8e8db2a9e07c4ee29c3cbb2411077f3b7))

## [0.14.1](https://github.com/input-output-hk/atala-prism-mediator/compare/prism-mediator-v0.14.0...prism-mediator-v0.14.1) (2024-02-20)


### Bug Fixes

* Support alias (DID) in Live mode ([#235](https://github.com/input-output-hk/atala-prism-mediator/issues/235)) ([2de77ed](https://github.com/input-output-hk/atala-prism-mediator/commit/2de77ed707e90c6356eaaf3ed9b7ced7325c8949)), closes [#230](https://github.com/input-output-hk/atala-prism-mediator/issues/230)

# [0.14.0](https://github.com/input-output-hk/atala-prism-mediator/compare/prism-mediator-v0.13.0...prism-mediator-v0.14.0) (2024-02-06)


### Bug Fixes

* re-enable the logging based on X-Request-Id header ([#211](https://github.com/input-output-hk/atala-prism-mediator/issues/211)) ([dee7dc2](https://github.com/input-output-hk/atala-prism-mediator/commit/dee7dc2bd9a0de689e2c6fb8141383aa7779d6a4))


### Features

* Update ScalaDID to 0.1.0-M18 and support new format of DID Peer ([#204](https://github.com/input-output-hk/atala-prism-mediator/issues/204)) ([26a1623](https://github.com/input-output-hk/atala-prism-mediator/commit/26a16230d7e7e68ccff92bc4f8fc1fd403254200)), closes [#158](https://github.com/input-output-hk/atala-prism-mediator/issues/158)

# [0.13.0](https://github.com/input-output-hk/atala-prism-mediator/compare/prism-mediator-v0.12.3...prism-mediator-v0.13.0) (2023-11-30)


### Bug Fixes

* register the transport ([#192](https://github.com/input-output-hk/atala-prism-mediator/issues/192)) ([e73a2ab](https://github.com/input-output-hk/atala-prism-mediator/commit/e73a2ab74c60916eec68f0415ba9a1d3da5c2587))


### Features

* Update timeout settings for websocket ([#191](https://github.com/input-output-hk/atala-prism-mediator/issues/191)) ([11a4437](https://github.com/input-output-hk/atala-prism-mediator/commit/11a44376664c55e97e18faa2b7f7849591b20532))

## [0.12.3](https://github.com/input-output-hk/atala-prism-mediator/compare/prism-mediator-v0.12.2...prism-mediator-v0.12.3) (2023-11-30)


### Bug Fixes

* force release.. ([#190](https://github.com/input-output-hk/atala-prism-mediator/issues/190)) ([b02f628](https://github.com/input-output-hk/atala-prism-mediator/commit/b02f628bd4a5d0ef1d234b5a3c211cfee96e17fa))

## [0.12.2](https://github.com/input-output-hk/atala-prism-mediator/compare/prism-mediator-v0.12.1...prism-mediator-v0.12.2) (2023-11-22)


### Bug Fixes

* websocket correct indentation ([#182](https://github.com/input-output-hk/atala-prism-mediator/issues/182)) ([c30fc1f](https://github.com/input-output-hk/atala-prism-mediator/commit/c30fc1f7f15ab3fc14cf0fc36bcf2fc5b5bdc0b6))

## [0.12.1](https://github.com/input-output-hk/atala-prism-mediator/compare/prism-mediator-v0.12.0...prism-mediator-v0.12.1) (2023-11-22)


### Bug Fixes

* Add and enable websocket on apisix route ([#179](https://github.com/input-output-hk/atala-prism-mediator/issues/179)) ([e5cfb1c](https://github.com/input-output-hk/atala-prism-mediator/commit/e5cfb1c0d5c284ab98ab36fef9e4bb81c97f3f53))
* correct endpoint for wss in deployment.yaml ([#178](https://github.com/input-output-hk/atala-prism-mediator/issues/178)) ([94866a9](https://github.com/input-output-hk/atala-prism-mediator/commit/94866a9077adf37761bd4b2218c280ef3bdc6e79))

# [0.12.0](https://github.com/input-output-hk/atala-prism-mediator/compare/prism-mediator-v0.11.0...prism-mediator-v0.12.0) (2023-11-22)


### Features

* websockets support ([#172](https://github.com/input-output-hk/atala-prism-mediator/issues/172)) ([30da5e7](https://github.com/input-output-hk/atala-prism-mediator/commit/30da5e7603e5373b96762214f82b0329a5d4000d))

# [0.11.0](https://github.com/input-output-hk/atala-prism-mediator/compare/prism-mediator-v0.10.2...prism-mediator-v0.11.0) (2023-11-10)


### Bug Fixes

* incorrect body of the message results in problem report ([#161](https://github.com/input-output-hk/atala-prism-mediator/issues/161)) ([6d8b9b8](https://github.com/input-output-hk/atala-prism-mediator/commit/6d8b9b8493f32d27c31c44c170268cdd3cb5170b))


### Features

* Return 202 HTTP code (for DID Comm) ([#162](https://github.com/input-output-hk/atala-prism-mediator/issues/162)) ([50185d3](https://github.com/input-output-hk/atala-prism-mediator/commit/50185d379d08949597479db8d8fd9d53526fd82a)), closes [#160](https://github.com/input-output-hk/atala-prism-mediator/issues/160)
* Support protocol discover-features 2.0 ([#154](https://github.com/input-output-hk/atala-prism-mediator/issues/154)) ([9220858](https://github.com/input-output-hk/atala-prism-mediator/commit/9220858111d992c099aff9e41cce90d7d06f0235))

## [0.10.2](https://github.com/input-output-hk/atala-prism-mediator/compare/prism-mediator-v0.10.1...prism-mediator-v0.10.2) (2023-11-06)


### Bug Fixes

* Send problemReport for duplicate message ([#157](https://github.com/input-output-hk/atala-prism-mediator/issues/157)) ([df522cf](https://github.com/input-output-hk/atala-prism-mediator/commit/df522cf57e550827dbfa3bff665c00b63051018e))

## [0.10.1](https://github.com/input-output-hk/atala-prism-mediator/compare/prism-mediator-v0.10.0...prism-mediator-v0.10.1) (2023-10-13)


### Bug Fixes

* allowed memory to increase gradually in CI ([#37](https://github.com/input-output-hk/atala-prism-mediator/issues/37)) ([a73abbd](https://github.com/input-output-hk/atala-prism-mediator/commit/a73abbdaf248eb474489e858c8b22913c1ec42e1))
* Discord links to never expires ([#141](https://github.com/input-output-hk/atala-prism-mediator/issues/141)) ([e7f7a74](https://github.com/input-output-hk/atala-prism-mediator/commit/e7f7a74237d5a79ab3fe39ff4f82712e1ed49163))
* **infra:** add sync-wave to certificate template ([#134](https://github.com/input-output-hk/atala-prism-mediator/issues/134)) ([82f902f](https://github.com/input-output-hk/atala-prism-mediator/commit/82f902f625246f0c1d0e06db096022e024c879c0))
* MissingProtocolExecuter ([#140](https://github.com/input-output-hk/atala-prism-mediator/issues/140)) ([c7a0f90](https://github.com/input-output-hk/atala-prism-mediator/commit/c7a0f90a094a5b075df9569a38f8f36681a25b2c))
* OutOfBand qrcode with base url ([#133](https://github.com/input-output-hk/atala-prism-mediator/issues/133)) ([56d34f9](https://github.com/input-output-hk/atala-prism-mediator/commit/56d34f9ebfee74b71a644b35761861fe49515861))
* send status message on delivery request If no messages are avail… ([#139](https://github.com/input-output-hk/atala-prism-mediator/issues/139)) ([c788335](https://github.com/input-output-hk/atala-prism-mediator/commit/c7883350f7e5b8070330010f8f299d4c0f32e7ee))

# [0.10.0](https://github.com/input-output-hk/atala-prism-mediator/compare/prism-mediator-v0.9.2...prism-mediator-v0.10.0) (2023-09-21)


### Bug Fixes

* Added DidSubject to report problem when not enrolled ([#111](https://github.com/input-output-hk/atala-prism-mediator/issues/111)) ([40c5d06](https://github.com/input-output-hk/atala-prism-mediator/commit/40c5d066840d2e0ae119fccee10d03bb10d7a5e9))
* Annotate Headers & Update zio-http to version 3.0.0-RC2 ([#106](https://github.com/input-output-hk/atala-prism-mediator/issues/106)) ([aa19f0a](https://github.com/input-output-hk/atala-prism-mediator/commit/aa19f0a1b9a547a562139b7c91373c1d84ddfa39))
* Encoder for VerificationMethodReferenced as String ([#110](https://github.com/input-output-hk/atala-prism-mediator/issues/110)) ([761d422](https://github.com/input-output-hk/atala-prism-mediator/commit/761d4227b8eef486fa12510814bb4c075f0b9c87))
* sign ProblemReport when TO is unspecified ([#105](https://github.com/input-output-hk/atala-prism-mediator/issues/105)) ([586dd9f](https://github.com/input-output-hk/atala-prism-mediator/commit/586dd9f1cce1763fd4cf5f1db93cf53f94740816))
* Update local docker compose configuration by adding the SERVICE_ENDPOINT port ([#128](https://github.com/input-output-hk/atala-prism-mediator/issues/128)) ([2adc6aa](https://github.com/input-output-hk/atala-prism-mediator/commit/2adc6aac26593e0f868dfee2e81afae7c4337b32))


### Features

* add MediatorBuildInfo & /did and /version endpoints ([#120](https://github.com/input-output-hk/atala-prism-mediator/issues/120)) ([4445e1f](https://github.com/input-output-hk/atala-prism-mediator/commit/4445e1f3db5011264e2831cf12d1ed183406447d))
* add tag latest to the mediator docker image ([#118](https://github.com/input-output-hk/atala-prism-mediator/issues/118)) ([020f2cd](https://github.com/input-output-hk/atala-prism-mediator/commit/020f2cdff5e8e32ac8f880b7a87f0e0239c5ce34))
* Added test for executor and problem reports  ([#117](https://github.com/input-output-hk/atala-prism-mediator/issues/117)) ([ce05d5a](https://github.com/input-output-hk/atala-prism-mediator/commit/ce05d5a0894ae0cee1eaba89bd5834182800e9ef))

## [0.9.2](https://github.com/input-output-hk/atala-prism-mediator/compare/prism-mediator-v0.9.1...prism-mediator-v0.9.2) (2023-08-30)


### Bug Fixes

* **mediator:** Enabled X-Request-Id ATL-5568 ([#104](https://github.com/input-output-hk/atala-prism-mediator/issues/104)) ([b0d4fee](https://github.com/input-output-hk/atala-prism-mediator/commit/b0d4feec19b3d4dc796e580789b163d2c3855d55))
* mongodb init script updated infrastructure mongodb yaml ([#94](https://github.com/input-output-hk/atala-prism-mediator/issues/94)) ([d9cc42a](https://github.com/input-output-hk/atala-prism-mediator/commit/d9cc42a319d505e37b5a9cdbe47802283537adaf))
* set default endpoint to localhost ([#102](https://github.com/input-output-hk/atala-prism-mediator/issues/102)) ([de8b702](https://github.com/input-output-hk/atala-prism-mediator/commit/de8b702e6fa70a6a8f5ee311c7a236f45d328c04))

## [0.9.1](https://github.com/input-output-hk/atala-prism-mediator/compare/prism-mediator-v0.9.0...prism-mediator-v0.9.1) (2023-08-22)


### Bug Fixes

* collectionName messages.outbound ([#92](https://github.com/input-output-hk/atala-prism-mediator/issues/92)) ([a1bd657](https://github.com/input-output-hk/atala-prism-mediator/commit/a1bd657df0cc12a30bcc910028a84104efb9f65a))

# [0.9.0](https://github.com/input-output-hk/atala-prism-mediator/compare/prism-mediator-v0.8.2...prism-mediator-v0.9.0) (2023-08-21)


### Features

* Store outbound messages ([#84](https://github.com/input-output-hk/atala-prism-mediator/issues/84)) ([3576656](https://github.com/input-output-hk/atala-prism-mediator/commit/3576656844834d0ecc6365b912dda6f383936d5f))

## [0.8.2](https://github.com/input-output-hk/atala-prism-mediator/compare/prism-mediator-v0.8.1...prism-mediator-v0.8.2) (2023-08-18)


### Bug Fixes

* alias list is empty in new accounts by default ([#87](https://github.com/input-output-hk/atala-prism-mediator/issues/87)) ([39484e6](https://github.com/input-output-hk/atala-prism-mediator/commit/39484e68b87bf2c20570c271b9ee8fd447471b9f))

## [0.8.1](https://github.com/input-output-hk/atala-prism-mediator/compare/prism-mediator-v0.8.0...prism-mediator-v0.8.1) (2023-08-18)


### Bug Fixes

* reply condition check ReturnRoute ([#88](https://github.com/input-output-hk/atala-prism-mediator/issues/88)) ([68c86c8](https://github.com/input-output-hk/atala-prism-mediator/commit/68c86c871b8300e08cadbaf64284dc4d0e4abb3f))

# [0.8.0](https://github.com/input-output-hk/atala-prism-mediator/compare/prism-mediator-v0.7.0...prism-mediator-v0.8.0) (2023-08-18)


### Features

* reply asynchronous unless return_route all ([#86](https://github.com/input-output-hk/atala-prism-mediator/issues/86)) ([6249f37](https://github.com/input-output-hk/atala-prism-mediator/commit/6249f3701e2247614a5b42042cdbd8e0ab4541bb))

# [0.7.0](https://github.com/input-output-hk/atala-prism-mediator/compare/prism-mediator-v0.6.0...prism-mediator-v0.7.0) (2023-08-18)


### Bug Fixes

* mediator test ([#71](https://github.com/input-output-hk/atala-prism-mediator/issues/71)) ([7572dcc](https://github.com/input-output-hk/atala-prism-mediator/commit/7572dcc5bbd8ec07ee452bfc044863e50324c909))
* parsing error when return_route is none ([#77](https://github.com/input-output-hk/atala-prism-mediator/issues/77)) ([02dde1e](https://github.com/input-output-hk/atala-prism-mediator/commit/02dde1efc3b051b8c65e0d819c8737ebe769a66c))
* UserAccountRepo.createOrFindDidAccount ([#69](https://github.com/input-output-hk/atala-prism-mediator/issues/69)) ([3526f0a](https://github.com/input-output-hk/atala-prism-mediator/commit/3526f0a358b9928d74d1600b5705d42e36c90791))


### Features

* Error handling and Send Problem Reports ([#65](https://github.com/input-output-hk/atala-prism-mediator/issues/65)) ([fe46055](https://github.com/input-output-hk/atala-prism-mediator/commit/fe460550e8f1906eeaf29eb8cec45f6170fe7cbd))

# [0.6.0](https://github.com/input-output-hk/atala-prism-mediator/compare/prism-mediator-v0.5.0...prism-mediator-v0.6.0) (2023-07-27)


### Features

* add helm-chart for mediator ([#64](https://github.com/input-output-hk/atala-prism-mediator/issues/64)) ([52e5d3b](https://github.com/input-output-hk/atala-prism-mediator/commit/52e5d3bf031895336279d6981016bada9ce32eaf)), closes [#61](https://github.com/input-output-hk/atala-prism-mediator/issues/61) [#63](https://github.com/input-output-hk/atala-prism-mediator/issues/63)
* helm-chart appVersion bump and addtional fixes ([#67](https://github.com/input-output-hk/atala-prism-mediator/issues/67)) ([3ccbe3e](https://github.com/input-output-hk/atala-prism-mediator/commit/3ccbe3ed093c7f22dbd4b934e9d9ce8488cbd302))

# [0.5.0](https://github.com/input-output-hk/atala-prism-mediator/compare/prism-mediator-v0.4.1...prism-mediator-v0.5.0) (2023-07-21)


### Features

* add endpoint to get the OOB mediate invitation ([#63](https://github.com/input-output-hk/atala-prism-mediator/issues/63)) ([c82282c](https://github.com/input-output-hk/atala-prism-mediator/commit/c82282ca8c7061cc1ec702af538ab77e2c9a1f3c))

## [0.4.1](https://github.com/input-output-hk/atala-prism-mediator/compare/prism-mediator-v0.4.0...prism-mediator-v0.4.1) (2023-07-20)


### Bug Fixes

* mediator ATL-4883 pickup status message to sync reply ([#59](https://github.com/input-output-hk/atala-prism-mediator/issues/59)) ([c0b6de0](https://github.com/input-output-hk/atala-prism-mediator/commit/c0b6de0f4fe4702641ff9e8a371b3aff3cd74e1f))

# [0.4.0](https://github.com/input-output-hk/atala-prism-mediator/compare/prism-mediator-v0.3.1...prism-mediator-v0.4.0) (2023-07-13)


### Bug Fixes

* Id/hash of the message must be deterministic ([#53](https://github.com/input-output-hk/atala-prism-mediator/issues/53)) ([d122b99](https://github.com/input-output-hk/atala-prism-mediator/commit/d122b993d54b3a8e557374709b9d8628c38ee74e))


### Features

* Better error handling for connection refused ([#47](https://github.com/input-output-hk/atala-prism-mediator/issues/47)) ([429940e](https://github.com/input-output-hk/atala-prism-mediator/commit/429940e2ef6807017c4e4ef156432e843c5cdccc))
* Do not send response errors to the caller ([#50](https://github.com/input-output-hk/atala-prism-mediator/issues/50)) ([60ee3ef](https://github.com/input-output-hk/atala-prism-mediator/commit/60ee3ef8e4342fb5fa69501502abdd739c55e22a))

## [0.3.1](https://github.com/input-output-hk/atala-prism-mediator/compare/prism-mediator-v0.3.0...prism-mediator-v0.3.1) (2023-07-10)


### Bug Fixes

* mediator oob webpage added logo  ([#42](https://github.com/input-output-hk/atala-prism-mediator/issues/42)) ([45debc8](https://github.com/input-output-hk/atala-prism-mediator/commit/45debc8c2d607cb298af0f1b047fb2083a334b71))

# [0.3.0](https://github.com/input-output-hk/atala-prism-mediator/compare/prism-mediator-v0.2.0...prism-mediator-v0.3.0) (2023-07-07)


### Bug Fixes

* dynamic DID on the web page ([#40](https://github.com/input-output-hk/atala-prism-mediator/issues/40)) ([0949a19](https://github.com/input-output-hk/atala-prism-mediator/commit/0949a191679caf91575a7d27d69ad1ced89577cd))
* mediator docker compose image repo and version ([#39](https://github.com/input-output-hk/atala-prism-mediator/issues/39)) ([116174b](https://github.com/input-output-hk/atala-prism-mediator/commit/116174ba616c31f5f28c099dfb1b04a360d258e0))
* mediator rename package and refactoring ([#41](https://github.com/input-output-hk/atala-prism-mediator/issues/41)) ([c755c99](https://github.com/input-output-hk/atala-prism-mediator/commit/c755c99f3547561b46d3b2cbac4e3cecc467d0c6))


### Features

* fix mongo config when no port & support gzip ([#35](https://github.com/input-output-hk/atala-prism-mediator/issues/35)) ([b2b2a02](https://github.com/input-output-hk/atala-prism-mediator/commit/b2b2a02261ffdd5a4362e4b8e28f34479f4eccef))

# [0.2.0](https://github.com/input-output-hk/atala-prism-mediator/compare/prism-mediator-v0.1.1...prism-mediator-v0.2.0) (2023-07-04)


### Bug Fixes

* disabled documentation generation ([#24](https://github.com/input-output-hk/atala-prism-mediator/issues/24)) ([8a2a6ad](https://github.com/input-output-hk/atala-prism-mediator/commit/8a2a6adf712e17caecdbd2b2f06ff5edf68e0a03))
* field name routing_did in keylist response body ([#22](https://github.com/input-output-hk/atala-prism-mediator/issues/22)) ([f61f114](https://github.com/input-output-hk/atala-prism-mediator/commit/f61f1148f033fa3587ab9787bb06428ba14cf6ab))
* Fix release job (add more ram) ([#32](https://github.com/input-output-hk/atala-prism-mediator/issues/32)) ([97ab05a](https://github.com/input-output-hk/atala-prism-mediator/commit/97ab05a943053dce1d789d6aac6d736517c2bfee))
* make content-type header case insensitive & remove support for ws ([#7](https://github.com/input-output-hk/atala-prism-mediator/issues/7)) ([d4a169a](https://github.com/input-output-hk/atala-prism-mediator/commit/d4a169a9ef16b4677bfb31b8785dc79474d9062a))
* mediator db not storing the original value for protected header ([#15](https://github.com/input-output-hk/atala-prism-mediator/issues/15)) ([bd119f1](https://github.com/input-output-hk/atala-prism-mediator/commit/bd119f162b1735d1e7c386e7e421877e19bec7b2))
* mediator unique constraint issue ([#25](https://github.com/input-output-hk/atala-prism-mediator/issues/25)) ([576d7a3](https://github.com/input-output-hk/atala-prism-mediator/commit/576d7a3090598eca325b7c5ddc9834298253ba8b))
* trustPing call back not working ([#23](https://github.com/input-output-hk/atala-prism-mediator/issues/23)) ([e8bf356](https://github.com/input-output-hk/atala-prism-mediator/commit/e8bf356de2b8143e7728e5414e6b2cfc24ae4957))


### Features

* Add config from application.conf and docker compose and an example ([#10](https://github.com/input-output-hk/atala-prism-mediator/issues/10)) ([2037377](https://github.com/input-output-hk/atala-prism-mediator/commit/203737789a53c9a22d0450564a988518c61f1fc0))
* Add Storage ([#8](https://github.com/input-output-hk/atala-prism-mediator/issues/8)) ([881e66e](https://github.com/input-output-hk/atala-prism-mediator/commit/881e66e9b6d0bbfc49cb0d8ec63583c802257a40))
* mediator  added more logs and associated message Hash and structured logging ([#16](https://github.com/input-output-hk/atala-prism-mediator/issues/16)) ([119b637](https://github.com/input-output-hk/atala-prism-mediator/commit/119b6372ab51ece6ded913b93ea7a607cde9acfe))
* mediator added test for storage layer ([#13](https://github.com/input-output-hk/atala-prism-mediator/issues/13)) ([25728b6](https://github.com/input-output-hk/atala-prism-mediator/commit/25728b6d8aad7cc2143844bad10e2e27bdf5d25f))
* mediator rename package and organise the packages ([#12](https://github.com/input-output-hk/atala-prism-mediator/issues/12)) ([8ea2aac](https://github.com/input-output-hk/atala-prism-mediator/commit/8ea2aaccadf515fe6a7d300c0250d70e38fb3b18))
* new a Webapp to show the QR Code  ([#21](https://github.com/input-output-hk/atala-prism-mediator/issues/21)) ([9af0a87](https://github.com/input-output-hk/atala-prism-mediator/commit/9af0a87cab64b62cb663bbe5bfedf730d09d50de))
* process KeylistQuery and return Keylist ([#30](https://github.com/input-output-hk/atala-prism-mediator/issues/30)) ([c5fb175](https://github.com/input-output-hk/atala-prism-mediator/commit/c5fb17584dc9d464e49589473ee3fe185db0b58f))
* reply to StatusRequest ([#26](https://github.com/input-output-hk/atala-prism-mediator/issues/26)) ([28ee891](https://github.com/input-output-hk/atala-prism-mediator/commit/28ee891999f5174356f245d46812526b044b789b))

## [0.1.1](https://github.com/input-output-hk/atala-prism-mediator/compare/prism-mediator-v0.1.0...prism-mediator-v0.1.1) (2023-05-23)


### Bug Fixes

* execute the ProtocolExecute's jobToRun zio ([#8](https://github.com/input-output-hk/atala-prism-mediator/issues/8)) ([5034499](https://github.com/input-output-hk/atala-prism-mediator/commit/503449991e10a78b82b1094d239703a0c9cd167b))
* Hardcode the atala prism did identity ([#9](https://github.com/input-output-hk/atala-prism-mediator/issues/9)) ([7984eea](https://github.com/input-output-hk/atala-prism-mediator/commit/7984eeacaf6bbd70a58356c3df74ce87b75485bd))

# [0.1.0](https://github.com/input-output-hk/atala-prism-mediator/compare/prism-mediator-v0.0.1...prism-mediator-v0.1.0) (2023-05-18)


### Bug Fixes

* sbt config enable docker plugin ([#6](https://github.com/input-output-hk/atala-prism-mediator/issues/6)) ([a102724](https://github.com/input-output-hk/atala-prism-mediator/commit/a102724bf9ed14f3a51b876c0e6acfbccbc96a6c))


### Features

* Add docker config to build.sbt ([#5](https://github.com/input-output-hk/atala-prism-mediator/issues/5)) ([c7418d0](https://github.com/input-output-hk/atala-prism-mediator/commit/c7418d0b0f17979d20af3ed6d7c8609ec2056c13))
* mediator initial release ([#4](https://github.com/input-output-hk/atala-prism-mediator/issues/4)) ([8499bf2](https://github.com/input-output-hk/atala-prism-mediator/commit/8499bf24d4d94ee30fb917501de4364d2cd6c96e))
