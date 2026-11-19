package org.hyperledger.identus.mediator

import com.typesafe.config.ConfigFactory
import fmgp.crypto.KeyStore
import fmgp.did.DIDSubject
import org.hyperledger.identus.mediator.db.AgentStub
import zio.*
import zio.config.typesafe.*
import zio.test.*

object MediatorConfigSpec extends ZIOSpecDefault {

  private val legacyKeyAgreement =
    AgentStub.keyAgreement("Z6D8LduZgZ6LnrOHPrMTS6uU2u5Btsrk1SGs4fn8M7c", "Sr4SkIskjN_VdKTn0zkjYbhGTWArdUNE4j_DmUpnQGw")

  private val legacyKeyAuthentication =
    AgentStub.keyAuthentication("INXCnxFEl0atLIIQYruHzGd5sUivMRyQOzu87qVerug", "MBjnXZxkMcoQVVL21hahWAw43RuAG-i64ipbeKKqwoA")

  private val legacyIdentity = """
    mediator.identity {
      endpoints = "http://localhost:8080"
      keyAgreement {
        kty = "OKP"
        crv = "X25519"
        d = "Z6D8LduZgZ6LnrOHPrMTS6uU2u5Btsrk1SGs4fn8M7c"
        x = "Sr4SkIskjN_VdKTn0zkjYbhGTWArdUNE4j_DmUpnQGw"
      }
      keyAuthentication {
        kty = "OKP"
        crv = "Ed25519"
        d = "INXCnxFEl0atLIIQYruHzGd5sUivMRyQOzu87qVerug"
        x = "MBjnXZxkMcoQVVL21hahWAw43RuAG-i64ipbeKKqwoA"
      }
    }
  """

  private def loadIdentity(hocon: String) =
    ConfigProvider
      .fromHoconString(hocon)
      .nested("identity")
      .nested("mediator")
      .load(MediatorConfig.configFor(ConfigFactory.parseString(hocon)))

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
    test("legacy HOCON configuration selects the peer identity") {
      loadIdentity(legacyIdentity).map(config => assertTrue(config.did.string.startsWith("did:peer:2.")))
    },
    test("invalid explicit DID fails instead of falling back to legacy keys") {
      loadIdentity(legacyIdentity + "\nmediator.identity.did = invalid-did").either.map { result =>
        assertTrue(result.fold(_.toString.contains("Fail to parse the DID"), _ => false))
      }
    },
    test("explicit DID without a keystore fails instead of falling back to legacy keys") {
      loadIdentity(legacyIdentity + s"\nmediator.identity.did = \"did:prism:${"a" * 64}\"").either.map { result =>
        assertTrue(result.isLeft)
      }
    },
    test("keystore without an explicit DID fails instead of falling back to legacy keys") {
      loadIdentity(legacyIdentity + "\nmediator.identity.keyStore = []").either.map { result =>
        assertTrue(result.isLeft)
      }
    },
    test("explicit DID with an empty keystore fails instead of starting without private keys") {
      val hocon = legacyIdentity + s"""
        mediator.identity.did = "did:prism:${"a" * 64}"
        mediator.identity.keyStore = []
      """
      loadIdentity(hocon).either.map { result =>
        assertTrue(result.fold(_.toString.contains("keyStore must not be empty"), _ => false))
      }
    },
    test("malformed explicit keystore fails instead of falling back to legacy keys") {
      val hocon = legacyIdentity + s"""
        mediator.identity.did = "did:prism:${"a" * 64}"
        mediator.identity.keyStore = [{ kty = "OKP", crv = "invalid", d = "bad", x = "bad", kid = "key-1" }]
      """
      loadIdentity(hocon).either.map(result => assertTrue(result.isLeft))
    },
    test("PRISM identity requires an operator-supplied resolver URL") {
      val peer = AgentStub.mediatorConfig
      val prism = MediatorConfig(DIDSubject(s"did:prism:${"a" * 64}").toDID, peer.keyStore)
      assertTrue(
        MediatorConfig.prismResolverUrl(peer, None) == Right(None),
        MediatorConfig.prismResolverUrl(prism, None).isLeft,
        MediatorConfig.prismResolverUrl(prism, Some("https://resolver.example/api/dids")) ==
          Right(Some("https://resolver.example/api/dids"))
      )
    },
    test("explicit HOCON identity selects the supplied PRISM DID and keys") {
      val did = s"did:prism:${"a" * 64}"
      val hocon = legacyIdentity + s"""
        mediator.identity.did = "$did"
        mediator.identity.keyStore = [
          { kty = "OKP", crv = "X25519", d = "Z6D8LduZgZ6LnrOHPrMTS6uU2u5Btsrk1SGs4fn8M7c", x = "Sr4SkIskjN_VdKTn0zkjYbhGTWArdUNE4j_DmUpnQGw", kid = "$did#key-1" },
          { kty = "OKP", crv = "Ed25519", d = "INXCnxFEl0atLIIQYruHzGd5sUivMRyQOzu87qVerug", x = "MBjnXZxkMcoQVVL21hahWAw43RuAG-i64ipbeKKqwoA", kid = "$did#key-2" }
        ]
      """
      loadIdentity(hocon).map(config => assertTrue(config.did.string == did, config.keyStore.keys.size == 2))
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
