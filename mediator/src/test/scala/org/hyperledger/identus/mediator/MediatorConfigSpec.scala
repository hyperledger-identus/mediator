package org.hyperledger.identus.mediator

import fmgp.crypto.KeyStore
import fmgp.did.DIDSubject
import org.hyperledger.identus.mediator.db.AgentStub
import zio.test.*

object MediatorConfigSpec extends ZIOSpecDefault {

  private val legacyKeyAgreement =
    AgentStub.keyAgreement("Z6D8LduZgZ6LnrOHPrMTS6uU2u5Btsrk1SGs4fn8M7c", "Sr4SkIskjN_VdKTn0zkjYbhGTWArdUNE4j_DmUpnQGw")

  private val legacyKeyAuthentication =
    AgentStub.keyAuthentication("INXCnxFEl0atLIIQYruHzGd5sUivMRyQOzu87qVerug", "MBjnXZxkMcoQVVL21hahWAw43RuAG-i64ipbeKKqwoA")

  override def spec = suite("MediatorConfigSpec")(
    test("legacy configuration still generates a did:peer mediator identity") {
      val config = MediatorConfig.legacy(
        endpoints = "http://localhost:8080;ws://localhost:8080/ws",
        keyAgreement = legacyKeyAgreement,
        keyAuthentication = legacyKeyAuthentication
      )

      assertTrue(config.did.string.startsWith("did:peer:2.")) &&
      assertTrue(config.keyStore.keys.size == 2)
    },
    test("explicit configuration accepts a did:prism identity with an operator-supplied keystore") {
      val did = DIDSubject(s"did:prism:${"a" * 64}").toDID
      val keyStore = KeyStore(
        Set(
          legacyKeyAgreement.withKid(s"${did.string}#key-1"),
          legacyKeyAuthentication.withKid(s"${did.string}#key-2"),
        )
      )

      val config = MediatorConfig(did = did, keyStore = keyStore)

      assertTrue(config.did.string == did.string) &&
      assertTrue(config.keyStore.keys.size == 2)
    }
  )
}
