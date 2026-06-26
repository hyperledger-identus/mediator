package org.hyperledger.identus.mediator

import fmgp.crypto.error.{DidFail, ValidationFailed}
import fmgp.did.*
import fmgp.did.comm.Operations
import fmgp.did.comm.protocol.ProtocolExecuter
import fmgp.did.comm.protocol.reportproblem2.{ProblemCode, ProblemReport, toProblemReport}
import fmgp.did.comm.{EncryptedMessage, PlaintextMessage, SignedMessage}
import fmgp.did.framework.*
import fmgp.did.method.peer.DidPeerResolver
import fmgp.did.comm.layerOperations
import org.hyperledger.identus.mediator.db.AgentStub
import org.hyperledger.identus.mediator.protocols.Problems
import zio.*
import zio.json.*
import zio.stream.{ZSink, ZStream}
import zio.test.*

object AgentExecutorMediatorSpec extends ZIOSpecDefault {
  private val mediatorAgent = MediatorAgent(AgentStub.mediatorConfig.did, AgentStub.mediatorConfig.keyStore)

  override def spec =
    suite("AgentExecutorMediatorSpec")(
      test("resolver fallback wraps plaintext replies into a signed problem-report") {
        ZIO.scoped {
          for {
            transport <- captureTransport
            agentExecutor <- testExecutor
            plaintext = plaintextBasicMessage
            _ <- invokeHandleResolverFailure(agentExecutor, Right(plaintext), transport, ValidationFailed)
              .provideSomeLayer(DidPeerResolver.layerDidPeerResolver)
              .provideSomeLayer(layerOperations(Operations))
              .provideSomeLayer(ZLayer.succeed(mediatorAgent))
            sent <- transport.queue.take
            signed <- ZIO.fromOption(sent match
              case msg: SignedMessage => Some(msg)
              case _                  => None
            )
              .orElseFail(new RuntimeException(s"Expected SignedMessage, got ${sent.getClass.getSimpleName}"))
            payload <- ZIO.fromEither(signed.payloadAsPlaintextMessage).mapError(err => new RuntimeException(err.toString))
            report <- ZIO.fromOption(payload.toProblemReport.toOption)
              .orElseFail(new RuntimeException(s"Expected problem-report, got ${payload.`type`}"))
          } yield assertTrue(
            report.piuri == ProblemReport.piuri,
            report.code == ProblemCode.ErroFail("me", "res", "resolver"),
            report.comment.exists(_.contains("Unable to resolve DID while handling message"))
          )
        }
      },
      test("resolver fallback re-signs an existing problem-report") {
        ZIO.scoped {
          for {
            transport <- captureTransport
            agentExecutor <- testExecutor
            problemReport = Problems.decryptFail(AgentStub.mediatorConfig.did.asFROM, "decrypt failed")
            _ <- invokeHandleResolverFailure(agentExecutor, Left(problemReport), transport, ValidationFailed)
              .provideSomeLayer(DidPeerResolver.layerDidPeerResolver)
              .provideSomeLayer(layerOperations(Operations))
              .provideSomeLayer(ZLayer.succeed(mediatorAgent))
            sent <- transport.queue.take
            signed <- ZIO.fromOption(sent match
              case msg: SignedMessage => Some(msg)
              case _                  => None
            )
              .orElseFail(new RuntimeException(s"Expected SignedMessage, got ${sent.getClass.getSimpleName}"))
            payload <- ZIO.fromEither(signed.payloadAsPlaintextMessage).mapError(err => new RuntimeException(err.toString))
            report <- ZIO.fromOption(payload.toProblemReport.toOption)
              .orElseFail(new RuntimeException(s"Expected problem-report, got ${payload.`type`}"))
          } yield assertTrue(
            report.comment.contains("decrypt failed"),
            report.code == ProblemCode.ErroFail("msg")
          )
        }
      }
    )

  private case class CaptureTransport(
      queue: Queue[SignedMessage | EncryptedMessage],
      transport: TransportDIDComm[Any]
  )

  private def captureTransport: UIO[CaptureTransport] =
    for {
      outboundQueue <- Queue.unbounded[SignedMessage | EncryptedMessage]
    } yield CaptureTransport(
      queue = outboundQueue,
      transport = new TransportDIDComm[Any] {
        def transmissionFlow = Transport.TransmissionFlow.BothWays
        def transmissionType = Transport.TransmissionType.SingleTransmission
        def id: TransportID = "capture-transport"
        def inbound = ZStream.empty
        def outbound = ZSink.fromQueue(outboundQueue)
      }
    )

  private def testExecutor: ZIO[Scope, Nothing, AgentExecutorMediator] =
    for {
      transportManager <- Ref.make(
        MediatorTransportManager(
          transportFactory = new TransportFactory {
            override def openTransport(uri: String): UIO[TransportDIDComm[Any]] =
              ZIO.dieMessage(s"Unexpected openTransport($uri) in AgentExecutorMediatorSpec")
          }
        )
      )
      scope <- ZIO.service[Scope]
    } yield AgentExecutorMediator(
      agent = mediatorAgent,
      transportManager = transportManager,
      protocolHandler = null.asInstanceOf[ProtocolExecuter[OperatorImp.Services, MediatorError | StorageError]],
      userAccountRepo = null.asInstanceOf[org.hyperledger.identus.mediator.db.UserAccountRepo],
      messageItemRepo = null.asInstanceOf[org.hyperledger.identus.mediator.db.MessageItemRepo],
      scope = scope
    )

  private def invokeHandleResolverFailure(
      agentExecutor: AgentExecutorMediator,
      input: Either[ProblemReport, PlaintextMessage],
      transport: CaptureTransport,
      didFail: DidFail
  ): ZIO[Agent & Operations & Resolver, Nothing, Unit] = {
    val method = classOf[AgentExecutorMediator].getDeclaredMethods.find(_.getName.contains("handleResolverFailure")).get
    method.setAccessible(true)
    method
      .invoke(agentExecutor, input.asInstanceOf[AnyRef], transport.transport.asInstanceOf[AnyRef], didFail.asInstanceOf[AnyRef])
      .asInstanceOf[ZIO[Agent & Operations & Resolver, Nothing, Unit]]
  }

  private def plaintextBasicMessage: PlaintextMessage =
    s"""{
       |  "id" : "resolver-fallback-test",
       |  "type" : "https://didcomm.org/basicmessage/2.0/message",
       |  "to" : [
       |     "${AgentStub.mediatorConfig.did.string}"
       |  ],
       |  "from" : "${AgentStub.bobAgent.id.string}",
       |  "body" : {
       |    "content" : "hello"
       |  },
       |  "return_route" : "all",
       |  "typ" : "application/didcomm-plain+json"
       |}""".stripMargin.fromJson[PlaintextMessage].toOption.get
}
