package org.hyperledger.identus.mediator.protocols

import fmgp.did.comm.protocol.reportproblem2.*
import fmgp.did.comm.protocol.*
import fmgp.did.comm.{EncryptedMessage, Operations, PlaintextMessage, SignedMessage, layerOperations}
import fmgp.did.method.peer.DidPeerResolver
import fmgp.util.Base64
import org.hyperledger.identus.mediator.db.*
import org.hyperledger.identus.mediator.db.MessageItemRepoSpec.encryptedMessageAlice
import org.hyperledger.identus.mediator.protocols.*
import zio.*
import zio.ExecutionStrategy.Sequential
import zio.http.Client
import zio.json.*
import zio.test.*
import zio.test.Assertion.*
import fmgp.did.{Agent, DIDSubject}
import org.hyperledger.identus.mediator.MediatorAgent

import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.api.bson.BSONDocument
import Operations.*
import fmgp.did.comm.protocol.mediatorcoordination2.*
import org.hyperledger.identus.mediator.db.AgentStub.*
import org.hyperledger.identus.mediator.db.EmbeddedMongoDBInstance.*

/** mediator/testOnly org.hyperledger.identus.mediator.protocols.MediatorCoordinationExecuterSpec */
object MediatorCoordinationExecuterSpec extends ZIOSpecDefault with DidAccountStubSetup with MessageSetup {

  override def spec = {

    suite("MediatorCoordinationExecuterSpec")(
      test("MediationRequest message for enrolling new DID should get mediation grant") {
        val executer = MediatorCoordinationExecuter
        for {
          agent <- ZIO.service[MediatorAgent]
          msg <- ZIO.fromEither(plaintextMediationRequestMessage(bobAgent.id.did, agent.id.did))
          action <- executer.program(msg)
        } yield {
          action match
            case reply: AnyReply => assertTrue(reply.msg.`type` == MediateGrant.piuri)
            case _               => assertTrue(false)
        }
      } @@ TestAspect.before(setupAndClean),
      test("MediationRequest message for already used alias did should get mediation deny") {
        val executer = MediatorCoordinationExecuter
        for {
          userAccount <- ZIO.service[UserAccountRepo]
          agent <- ZIO.service[MediatorAgent]
          result <- userAccount.createOrFindDidAccount(alice)
          result <- userAccount.addAlias(owner = alice, newAlias = DIDSubject(aliceAgent.id.did))
          msg <- ZIO.fromEither(plaintextMediationRequestMessage(aliceAgent.id.did, agent.id.did))
          action <- executer.program(msg)
        } yield {
          action match
            case reply: AnyReply => assertTrue(reply.msg.`type` == MediateDeny.piuri)
            case _               => assertTrue(false)
        }
      } @@ TestAspect.before(setupAndClean),
      test("KeyList Update message Request should add alias and return keyList Response") {
        val executer = MediatorCoordinationExecuter
        for {
          userAccount <- ZIO.service[UserAccountRepo]
          agent <- ZIO.service[MediatorAgent]
          result <- userAccount.createOrFindDidAccount(DIDSubject(aliceAgent.id.did))
          msg <- ZIO.fromEither(
            plaintextKeyListUpdateRequestMessage(aliceAgent.id.did, agent.id.did, aliceAgent.id.did)
          )
          action <- executer.program(msg)
        } yield {
          action match
            case reply: AnyReply => assertTrue(reply.msg.`type` == KeylistResponse.piuri)
            case _               => assertTrue(false)
        }
      } @@ TestAspect.before(setupAndClean),
      test("KeyList remove alias message Request should remove alias and return keyList Response") {
        val executer = MediatorCoordinationExecuter
        for {
          userAccount <- ZIO.service[UserAccountRepo]
          agent <- ZIO.service[MediatorAgent]
          result <- userAccount.createOrFindDidAccount(DIDSubject(aliceAgent.id.did))
          result <- userAccount.addAlias(owner = DIDSubject(aliceAgent.id.did), newAlias = DIDSubject(bobAgent.id.did))
          msg <- ZIO.fromEither(
            plaintextKeyListRemoveAliasRequestMessage(aliceAgent.id.did, agent.id.did, bobAgent.id.did)
          )
          action <- executer.program(msg)
        } yield {
          action match
            case reply: AnyReply => assertTrue(reply.msg.`type` == KeylistResponse.piuri)
            case _               => assertTrue(false)
        }
      } @@ TestAspect.before(setupAndClean),
      test("KeyList remove alias non existing didAccount Request should return problem report") {
        val executer = MediatorCoordinationExecuter
        for {
          userAccount <- ZIO.service[UserAccountRepo]
          agent <- ZIO.service[MediatorAgent]
          result <- userAccount.createOrFindDidAccount(DIDSubject(aliceAgent.id.did))
          msg <- ZIO.fromEither(
            plaintextKeyListRemoveAliasRequestMessage(bobAgent.id.did, agent.id.did, bobAgent.id.did)
          )
          action <- executer.program(msg)
        } yield {
          action match
            case reply: AnyReply =>
              assert(reply.msg.toProblemReport)(
                isRight(
                  hasField("code", (p: ProblemReport) => p.code, equalTo(ProblemCode.ErroFail("req", "not_enroll"))) &&
                    hasField(
                      "from",
                      (p: ProblemReport) => p.from,
                      equalTo(agent.id.did)
                    )
                )
              )
            case _ => assertTrue(false)
        }
      } @@ TestAspect.before(setupAndClean)
    ).provideSomeLayer(DidPeerResolver.layerDidPeerResolver)
      .provideSomeLayer(Operations.layerOperations)
      .provideSomeLayer(DidPeerResolver.layerDidPeerResolver)
      .provideSomeLayer(AgentStub.agentLayer)
      .provideLayerShared(dataAccessLayer) @@ TestAspect.sequential
  }

  val dataAccessLayer = EmbeddedMongoDBInstance.layer(port, hostIp)
    >>> AsyncDriverResource.layer
    >>> ReactiveMongoApi.layer(connectionString)
    >>> (UserAccountRepo.layer ++ MessageItemRepo.layer ++ OutboxMessageRepo.layer)

}
