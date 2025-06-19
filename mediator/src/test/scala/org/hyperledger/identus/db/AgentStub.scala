package org.hyperledger.identus.mediator.db
import fmgp.crypto.{Curve, KTY, OKPPrivateKey, OKPPrivateKeyWithoutKid}
import fmgp.did.Agent
import fmgp.did.comm.EncryptedMessage
import fmgp.did.method.peer.{DIDPeer2, DIDPeerServiceEncoded}
import org.hyperledger.identus.mediator.{MediatorAgent, MediatorConfig}
import zio.{ULayer, ZLayer}

import java.net.URI

object AgentStub {

  def keyAgreement(d: String, x: String): OKPPrivateKeyWithoutKid =
    OKPPrivateKeyWithoutKid(kty = KTY.OKP, crv = Curve.X25519, d = d, x = x)

  def keyAuthentication(d: String, x: String): OKPPrivateKeyWithoutKid =
    OKPPrivateKeyWithoutKid(kty = KTY.OKP, crv = Curve.Ed25519, d = d, x = x)

  val endpoints = "http://localhost:8080"
  val mediatorConfig = MediatorConfig(
    endpoints,
    keyAgreement("Z6D8LduZgZ6LnrOHPrMTS6uU2u5Btsrk1SGs4fn8M7c", "Sr4SkIskjN_VdKTn0zkjYbhGTWArdUNE4j_DmUpnQGw"),
    keyAuthentication("INXCnxFEl0atLIIQYruHzGd5sUivMRyQOzu87qVerug", "MBjnXZxkMcoQVVL21hahWAw43RuAG-i64ipbeKKqwoA")
  )

  val endpointBob = "http://localhost:8081"
  val bobAgent = DIDPeer2.makeAgent(
    Seq(
      keyAgreement("H5wHQcecUqobAMT3RiNsAaYaFXIfTLCNhWAYXgTYv7E", "f8ce_zxdhIEy76JE21XpVDviRtR2amXaZ6NjYyIPjg4"),
      keyAuthentication("LyMSyr_usdn3pHZc00IbJaS2RcvF4OcJTJIB2Vw6dLQ", "TQdV8Wduyz3OylN3YbyHR0R-aynF3C1tmvHAgl6b34I")
    ),
    Seq(DIDPeerServiceEncoded.fromEndpoint(endpointBob))
  )

  val endpointAlice = "http://localhost:8081"
  val aliceAgent = DIDPeer2.makeAgent(
    Seq(
      keyAgreement("Z6D8LduZgZ6LnrOHPrMTS6uU2u5Btsrk1SGs4fn8M7c", "Sr4SkIskjN_VdKTn0zkjYbhGTWArdUNE4j_DmUpnQGw"),
      keyAuthentication("INXCnxFEl0atLIIQYruHzGd5sUivMRyQOzu87qVerug", "MBjnXZxkMcoQVVL21hahWAw43RuAG-i64ipbeKKqwoA")
    ),
    Seq(DIDPeerServiceEncoded.fromEndpoint(endpointAlice))
  )

  def aliceAgentLayer: ULayer[Agent] = ZLayer.succeed(aliceAgent)

  def bobAgentLayer: ULayer[Agent] = ZLayer.succeed(bobAgent)

  /** Mediator */
  val agentLayer = mediatorConfig.agentLayer

}
