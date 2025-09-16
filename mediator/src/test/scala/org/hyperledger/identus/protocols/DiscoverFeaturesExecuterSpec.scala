package org.hyperledger.identus.mediator.protocols

import fmgp.did.DIDSubject
import fmgp.did.comm.protocol.reportproblem2.{ProblemCode, ProblemReport}
import fmgp.did.comm.protocol.discoverfeatures2.*
import fmgp.did.comm.protocol.*
import fmgp.did.comm.{EncryptedMessage, Operations, PlaintextMessage, SignedMessage, layerOperations}
import fmgp.did.method.peer.DidPeerResolver
import fmgp.util.Base64
import org.hyperledger.identus.mediator.db.*
import org.hyperledger.identus.mediator.db.MessageItemRepoSpec.encryptedMessageAlice
import org.hyperledger.identus.mediator.protocols.DiscoverFeaturesExecuter
import zio.*
import zio.ExecutionStrategy.Sequential
import zio.http.Client
import zio.json.*
import zio.test.*
import zio.test.Assertion.*
import fmgp.did.comm.FROM
import fmgp.did.comm.TO

import scala.concurrent.ExecutionContext.Implicits.global
import org.hyperledger.identus.mediator.db.EmbeddedMongoDBInstance.*
import reactivemongo.api.bson.BSONDocument
import fmgp.did.DIDSubject.*
import fmgp.did.comm.Operations.authDecrypt
import org.hyperledger.identus.mediator.MediatorAgent
import org.hyperledger.identus.mediator.db.AgentStub.{bobAgent, bobAgentLayer}

/** mediator/testOnly org.hyperledger.identus.mediator.protocols.DiscoverFeaturesExecuterSpec */
object DiscoverFeaturesExecuterSpec extends ZIOSpecDefault with DidAccountStubSetup with MessageSetup {

  override def spec = suite("DiscoverFeaturesExecuterSpec")(
    test("DiscoverFeatures Query message should return the disclose message containing the matched protocols") {
      val executer = DiscoverFeaturesExecuter
      for {
        agent <- ZIO.service[MediatorAgent]
        msg <- ZIO.fromEither(plaintextDiscoverFeatureRequestMessage(bobAgent.id.did, agent.id.did))
        action <- executer.program(msg)
      } yield {
        action match
          case reply: AnyReply =>
            reply.msg.toFeatureDisclose match
              case Left(value) => assertTrue(false)
              case Right(featureDisclose) =>
                assertTrue(featureDisclose.disclosures.nonEmpty) &&
                assertTrue(featureDisclose.disclosures.head.id.contains("routing"))
          case _ => assertTrue(false)
      }
    } @@ TestAspect.before(setupAndClean),
    test(
      "DiscoverFeatures Query message doesn't match regex pattern, it should yield a disclose message with an empty body"
    ) {
      val executer = DiscoverFeaturesExecuter
      for {
        agent <- ZIO.service[MediatorAgent]
        msg <- ZIO.fromEither(plaintextDiscoverFeatureRequestMessageNoMatch(bobAgent.id.did, agent.id.did))
        action <- executer.program(msg)
      } yield {
        action match
          case reply: AnyReply =>
            reply.msg.toFeatureDisclose match
              case Left(value) => assertTrue(false)
              case Right(featureDisclose) =>
                assertTrue(featureDisclose.disclosures.isEmpty)
          case _ => assertTrue(false)
      }
    } @@ TestAspect.before(setupAndClean),
  ).provideSomeLayer(DidPeerResolver.layerDidPeerResolver)
    .provideSomeLayer(Operations.layerOperations)
    .provideSomeLayer(DidPeerResolver.layerDidPeerResolver)
    .provideSomeLayer(AgentStub.agentLayer)
    .provideLayerShared(dataAccessLayer) @@ TestAspect.sequential

  val dataAccessLayer = EmbeddedMongoDBInstance.layer(port, hostIp)
    >>> AsyncDriverResource.layer
    >>> ReactiveMongoApi.layer(connectionString)
    >>> (UserAccountRepo.layer ++ MessageItemRepo.layer ++ OutboxMessageRepo.layer)

}
