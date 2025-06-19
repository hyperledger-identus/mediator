package org.hyperledger.identus.mediator.protocols

import fmgp.did.comm.Operations.*
import fmgp.did.comm.protocol.pickup3.*
import fmgp.did.comm.protocol.reportproblem2.{ProblemCode, ProblemReport}
import fmgp.did.comm.*
import fmgp.did.comm.protocol.pickup3.StatusRequest
import fmgp.did.method.peer.DidPeerResolver
import fmgp.did.{Agent, DIDSubject}
import fmgp.util.Base64
import org.hyperledger.identus.mediator.*
import org.hyperledger.identus.mediator.db.*
import org.hyperledger.identus.mediator.db.AgentStub.*
import org.hyperledger.identus.mediator.db.EmbeddedMongoDBInstance.*
import org.hyperledger.identus.mediator.db.MessageItemRepoSpec.encryptedMessageAlice
import org.hyperledger.identus.mediator.protocols.*
import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.indexes.{Index, IndexType}
import zio.*
import zio.ExecutionStrategy.Sequential
import zio.http.Client
import zio.json.*
import zio.test.*
import zio.test.Assertion.*

import scala.concurrent.ExecutionContext.Implicits.global
import fmgp.did.comm.protocol.*

/** mediator/testOnly org.hyperledger.identus.mediator.protocols.PickupExecuterSpec */
object PickupExecuterSpec extends ZIOSpecDefault with DidAccountStubSetup with MessageSetup {

  override def spec = {

    suite("PickupExecuterSpec")(
      test("Pickup Status message should return ProblemReport") {
        val executer = PickupExecuter
        for {
          mediatorAgent <- ZIO.service[MediatorAgent]
          userAccount <- ZIO.service[UserAccountRepo]
          _ <- userAccount.createOrFindDidAccount(DIDSubject(aliceAgent.id.did))
          _ <- userAccount.addAlias(
            owner = DIDSubject(aliceAgent.id.did),
            newAlias = DIDSubject(aliceAgent.id.did)
          )
          msg <- ZIO.fromEither(plaintextStatusMessage(aliceAgent.id.did, mediatorAgent.id.did))
          action <- executer.program(msg)
        } yield {
          action match
            case reply: AnyReply => assertTrue(reply.msg.`type` == ProblemReport.piuri)
            case _               => assertTrue(false)
        }
      },
      test("Pickup StatusRequest message should return problem report for not enrolled did") {
        val executer = PickupExecuter
        for {
          mediatorAgent <- ZIO.service[MediatorAgent]
          userAccount <- ZIO.service[UserAccountRepo]
          msg <- ZIO.fromEither(plaintextStatusRequestMessage(aliceAgent.id.did, mediatorAgent.id.did))
          action <- executer.program(msg)
        } yield {
          action match
            case reply: AnyReply => assertTrue(reply.msg.`type` == ProblemReport.piuri)
            case _               => assertTrue(false)
        }
      },
      test("Pickup StatusRequest message should return Status Message") {
        val executer = PickupExecuter
        for {
          mediatorAgent <- ZIO.service[MediatorAgent]
          userAccount <- ZIO.service[UserAccountRepo]
          _ <- userAccount.createOrFindDidAccount(DIDSubject(aliceAgent.id.did))
          _ <- userAccount.addAlias(
            owner = DIDSubject(aliceAgent.id.did),
            newAlias = DIDSubject(aliceAgent.id.did)
          )
          msg <- ZIO.fromEither(plaintextStatusRequestMessage(aliceAgent.id.did, mediatorAgent.id.did))
          action <- executer.program(msg)
        } yield {
          action match
            case reply: AnyReply => assertTrue(reply.msg.`type` == Status.piuri)
            case _               => assertTrue(false)
        }
      },
      test("Pickup DeliveryRequest message return MessageDelivery and attachment message") {
        val executer = PickupExecuter
        val forwardMessageExecuter = ForwardMessageExecuter
        for {
          mediatorAgent <- ZIO.service[MediatorAgent]
          userAccount <- ZIO.service[UserAccountRepo]
          _ <- userAccount.createOrFindDidAccount(DIDSubject(aliceAgent.id.did))
          _ <- userAccount.addAlias(
            owner = DIDSubject(aliceAgent.id.did),
            newAlias = DIDSubject(aliceAgent.id.did)
          )
          basicMsg <- ZIO.fromEither(plainTextBasicMessage(bobAgent.id.did, aliceAgent.id.did))
          encryptedBasicMessage <- authEncrypt(basicMsg).provideSomeLayer(
            bobAgentLayer
          )
          msgForward <- ZIO.fromEither(
            plaintextForwardMessage(aliceAgent.id.did, mediatorAgent.id.did, encryptedBasicMessage.toJson)
          )
          _ <- forwardMessageExecuter.program(msgForward)
          msg <- ZIO.fromEither(
            plaintextDeliveryRequestMessage(aliceAgent.id.did, mediatorAgent.id.did, aliceAgent.id.did)
          )
          action <- executer.program(msg)
        } yield {
          action match
            case reply: AnyReply =>
              assertTrue(reply.msg.`type` == MessageDelivery.piuri) && assertTrue(reply.msg.attachments.nonEmpty)
            case _ => assertTrue(false)
        }
      },
      test("Delivery Request message for Pickup returns a Status Message when there are no messages available") {
        val pickupExecuter = PickupExecuter
        for {
          mediatorAgent <- ZIO.service[MediatorAgent]
          userAccount <- ZIO.service[UserAccountRepo]
          _ <- userAccount.createOrFindDidAccount(DIDSubject(aliceAgent.id.did))
          _ <- userAccount.addAlias(
            owner = DIDSubject(aliceAgent.id.did),
            newAlias = DIDSubject(aliceAgent.id.did)
          )
          msg <- ZIO.fromEither(
            plaintextDeliveryRequestMessage(aliceAgent.id.did, mediatorAgent.id.did, aliceAgent.id.did)
          )
          action <- pickupExecuter.program(msg)
        } yield {
          action match
            case reply: AnyReply => assertTrue(reply.msg.`type` == Status.piuri)
            case _               => assertTrue(false)
        }
      },
      test("Messages Received message should clear the messages from the queue") {
        val executer = PickupExecuter
        val forwardMessageExecuter = ForwardMessageExecuter
        for {
          mediatorAgent <- ZIO.service[MediatorAgent]
          userAccount <- ZIO.service[UserAccountRepo]
          _ <- userAccount.createOrFindDidAccount(DIDSubject(aliceAgent.id.did))
          _ <- userAccount.addAlias(
            owner = DIDSubject(aliceAgent.id.did),
            newAlias = DIDSubject(aliceAgent.id.did)
          )
          basicMsg <- ZIO.fromEither(plainTextBasicMessage(bobAgent.id.did, aliceAgent.id.did))
          encryptedBasicMessage <- authEncrypt(basicMsg).provideSomeLayer(
            bobAgentLayer
          )
          msgForward <- ZIO.fromEither(
            plaintextForwardMessage(aliceAgent.id.did, mediatorAgent.id.did, encryptedBasicMessage.toJson)
          )
          _ <- forwardMessageExecuter.program(msgForward)
          msg <- ZIO.fromEither(
            plaintextDeliveryRequestMessage(aliceAgent.id.did, mediatorAgent.id.did, aliceAgent.id.did)
          )
          action1 <- executer.program(msg)
          // plainText = decryptedMessage.asInstanceOf[PlaintextMessage]
          attchmentID = action1.asInstanceOf[AnyReply].msg.attachments.map(_.flatMap(_.id).head).get
          messagesReceived <- ZIO.fromEither(
            plaintextMessagesReceivedRequestMessage(aliceAgent.id.did, mediatorAgent.id.did, attchmentID)
          )
          action2 <- executer.program(messagesReceived)
        } yield {
          action2 match
            case reply: AnyReply => assertTrue(false)
            case NoReply         => assertTrue(true)
        }
      },
      test("Pickup LiveMode over WS message should return Status Message") {
        val executer = PickupExecuter
        for {
          mediatorAgent <- ZIO.service[MediatorAgent]
          userAccount <- ZIO.service[UserAccountRepo]
          _ <- userAccount.createOrFindDidAccount(DIDSubject(aliceAgent.id.did))
          _ <- userAccount.addAlias(
            owner = DIDSubject(aliceAgent.id.did),
            newAlias = DIDSubject(aliceAgent.id.did)
          )
          msg <- ZIO.fromEither(plaintextLiveModeEnable(aliceAgent.id.did, mediatorAgent.id.did))
          action <- executer.program(msg)
        } yield {
          action match
            case reply: AnyReply => assertTrue(reply.msg.`type` == Status.piuri)
            case _               => assertTrue(false)
        }
      }
        .provideSomeLayer(ZLayer.succeed(TransportUtil.newTransportEmptyMultiTransmissions)),
      test("Pickup LiveMode over SingleTransmission (HTTP) message should return ProblemReport") {
        val executer = PickupExecuter
        for {
          mediatorAgent <- ZIO.service[MediatorAgent]
          userAccount <- ZIO.service[UserAccountRepo]
          _ <- userAccount.createOrFindDidAccount(DIDSubject(aliceAgent.id.did))
          _ <- userAccount.addAlias(
            owner = DIDSubject(aliceAgent.id.did),
            newAlias = DIDSubject(aliceAgent.id.did)
          )
          msg <- ZIO.fromEither(plaintextLiveModeEnable(aliceAgent.id.did, mediatorAgent.id.did))
          action <- executer.program(msg)
        } yield {
          action match
            case reply: AnyReply => assertTrue(reply.msg.`type` == ProblemReport.piuri)
            case _               => assertTrue(false)
        }
      }
        .provideSomeLayer(ZLayer.succeed(TransportUtil.newTransportEmptySingleTransmission)),
    ) @@ TestAspect.sequential @@ TestAspect.before(setupAndClean)
  }
    .provideSomeLayer(MediatorTransportManagerUtil.layerTest)
    .provideSomeLayer(DidPeerResolver.layerDidPeerResolver)
    .provideSomeLayer(Operations.layerOperations)
    .provideSomeLayer(DidPeerResolver.layerDidPeerResolver)
    .provideSomeLayer(AgentStub.agentLayer)
    .provideSomeLayer(ZLayer.succeed(TransportUtil.newTransportEmpty))
    .provideLayerShared(dataAccessLayer)

  val dataAccessLayer = EmbeddedMongoDBInstance.layer(port, hostIp)
    >>> AsyncDriverResource.layer
    >>> ReactiveMongoApi.layer(connectionString)
    >>> (UserAccountRepo.layer ++ MessageItemRepo.layer ++ OutboxMessageRepo.layer)

}
