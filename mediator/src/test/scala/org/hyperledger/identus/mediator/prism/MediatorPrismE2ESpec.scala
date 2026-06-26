package org.hyperledger.identus.mediator.prism

import com.google.protobuf.ByteString
import fmgp.crypto.*
import fmgp.did.*
import fmgp.did.comm.Operations
import fmgp.did.comm.Operations.*
import fmgp.did.comm.protocol.pickup3.MessageDelivery
import fmgp.did.comm.protocol.reportproblem2.ProblemReport
import fmgp.did.comm.protocol.reportproblem2.toProblemReport
import fmgp.did.comm.{Attachment, EncryptedMessage, MediaTypes, Message, PlaintextMessage, SignedMessage}
import fmgp.did.comm.layerOperations
import fmgp.did.framework.TransportFactoryImp
import fmgp.did.method.peer.DidPeerResolver
import fmgp.did.method.prism.{DIDPrismResolver, HttpUtils}
import fmgp.crypto.error.DidFail
import org.hyperledger.identus.apollo.derivation.HDKey
import org.hyperledger.identus.apollo.utils.{KMMEdKeyPair, KMMX25519KeyPair}
import org.hyperledger.identus.mediator.*
import org.hyperledger.identus.mediator.db.*
import org.hyperledger.identus.mediator.db.AgentStub
import proto.prism.{PrismOperation, SignedPrismOperation}
import proto.prism_ssi.{CompressedECKeyData, KeyUsage, ProtoCreateDID, PublicKey, Service}
import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.indexes.{Index, IndexType}
import zio.*
import zio.http.*
import zio.json.*
import zio.test.*
import zio.test.Assertion.*

import java.security.MessageDigest
import java.util.Base64

object MediatorPrismE2ESpec extends ZIOSpecDefault {

  private val prismE2eEnabled = sys.env.get("MEDIATOR_PRISM_E2E_ENABLED").contains("true")
  private val neoprismBaseUrl = sys.env.getOrElse("NEOPRISM_BASE_URL", "http://127.0.0.1:18081")
  private val neoprismResolverBaseUrl = s"$neoprismBaseUrl/api/dids"
  private val mongoConnectionString =
    sys.env.getOrElse("MEDIATOR_PRISM_MONGO_URI", "mongodb://127.0.0.1:27018/messages")
  private val mediatorPort = sys.env.get("MEDIATOR_PRISM_PORT").flatMap(_.toIntOption).getOrElse(18080)
  private val mediatorEndpoint = s"http://127.0.0.1:$mediatorPort"
  private val mongoLayer = AsyncDriverResource.layer
    >>> ReactiveMongoApi.layer(mongoConnectionString)
    >>> (UserAccountRepo.layer ++ MessageItemRepo.layer ++ OutboxMessageRepo.layer)
  private val clientResolverLayer = ZLayer.make[Client & Resolver](
    Client.default,
    Scope.default,
    resolverLayer(neoprismResolverBaseUrl)
  )

  private case class SubmitSignedOperationsRequest(signed_operations: Seq[String])
  private object SubmitSignedOperationsRequest {
    given JsonCodec[SubmitSignedOperationsRequest] = DeriveJsonCodec.gen[SubmitSignedOperationsRequest]
  }

  private case class PrismIdentity(
      shortForm: String,
      longForm: String,
      createOperation: PrismOperation,
      signedOperation: SignedPrismOperation,
      agent: TestAgent
  )

  private case class PrismIdentityOptions(
      serviceEndpoint: Option[String] = None,
      includeAuthenticationKey: Boolean = true,
      includeKeyAgreementKey: Boolean = true
  )

  private case class TestAgent(
      id: DID,
      keyStore: KeyStore
  ) extends Agent

  private val aliasIndex = Index(
    key = Seq("alias" -> IndexType.Ascending),
    name = Some("alias_did"),
    unique = true,
    background = true,
    partialFilter = Some(BSONDocument("alias.0" -> BSONDocument("$exists" -> true)))
  )

  private val prismE2eSpec =
    suite("MediatorPrismE2ESpec")(
      test("resolves PRISM long-form and short-form DIDs through NeoPRISM") {
        ZIO.scoped {
          for {
            _ <- waitForNeoPrism
            mediatorIdentity <- makeIdentity(PrismIdentityOptions(serviceEndpoint = Some(mediatorEndpoint)))
            senderIdentity <- makeIdentity()
            recipientIdentity <- makeIdentity()
            mediatorLongDocument <- resolveDidDocument(mediatorIdentity.longForm)
            senderLongDocument <- resolveDidDocument(senderIdentity.longForm)
            recipientLongDocument <- resolveDidDocument(recipientIdentity.longForm)
            _ <- publishAll(mediatorIdentity, senderIdentity, recipientIdentity)
            mediatorShortDocument <- resolveDidDocument(mediatorIdentity.shortForm)
            senderShortDocument <- resolveDidDocument(senderIdentity.shortForm)
            recipientShortDocument <- resolveDidDocument(recipientIdentity.shortForm)
          } yield assertTrue(
            documentJsonContains(mediatorLongDocument, mediatorIdentity.shortForm, mediatorEndpoint, "#auth-0", "#comm-0"),
            documentJsonContains(senderLongDocument, senderIdentity.shortForm, "#auth-0", "#comm-0"),
            documentJsonContains(recipientLongDocument, recipientIdentity.shortForm, "#auth-0", "#comm-0"),
            documentJsonContains(mediatorShortDocument, mediatorIdentity.shortForm, mediatorEndpoint, "#auth-0", "#comm-0"),
            documentJsonContains(senderShortDocument, senderIdentity.shortForm, "#auth-0", "#comm-0"),
            documentJsonContains(recipientShortDocument, recipientIdentity.shortForm, "#auth-0", "#comm-0")
          )
        }
      },
      test("mediates a basic DIDComm message using published PRISM DIDs") {
        ZIO.scoped {
          for {
            _ <- waitForNeoPrism
            mediatorIdentity <- makeIdentity(PrismIdentityOptions(serviceEndpoint = Some(mediatorEndpoint)))
            senderIdentity <- makeIdentity()
            recipientIdentity <- makeIdentity()
            _ <- publishAll(mediatorIdentity, senderIdentity, recipientIdentity)
            _ <- startMediatorServer(mediatorIdentity.agent)
            mediateGrant <- sendExpectReply(
              recipientIdentity.agent,
              plaintextMediationRequestMessage(recipientIdentity.shortForm, mediatorIdentity.shortForm)
            )
            _ <- ZIO.fail(new RuntimeException(s"Expected mediate-grant, got ${mediateGrant.`type`}"))
              .unless(mediateGrant.`type`.value == "https://didcomm.org/coordinate-mediation/2.0/mediate-grant")
            keylistResponse <- sendExpectReply(
              recipientIdentity.agent,
              plaintextKeyListUpdateRequestMessage(
                recipientIdentity.shortForm,
                mediatorIdentity.shortForm,
                recipientIdentity.shortForm
              )
            )
            _ <- ZIO.fail(new RuntimeException(s"Expected keylist-update-response, got ${keylistResponse.`type`}"))
              .unless(
                keylistResponse.`type`.value == "https://didcomm.org/coordinate-mediation/2.0/keylist-update-response"
              )
            basicMessage = plainTextBasicMessage(senderIdentity.shortForm, recipientIdentity.shortForm)
            encryptedBasic <- authEncrypt(basicMessage)
              .provideSomeLayer(ZLayer.succeed(senderIdentity.agent))
              .mapError(didFailAsThrowable)
            forwardMessage = plaintextForwardMessage(
              senderIdentity.shortForm,
              recipientIdentity.shortForm,
              mediatorIdentity.shortForm,
              encryptedBasic.toJson
            )
            _ <- sendWithoutReply(senderIdentity.agent, forwardMessage)
            delivery <- sendExpectReply(
              recipientIdentity.agent,
              plaintextDeliveryRequestMessage(
                recipientIdentity.shortForm,
                mediatorIdentity.shortForm,
                recipientIdentity.shortForm
              )
            )
            _ <- ZIO.fail(new RuntimeException(s"Expected message-delivery, got ${delivery.`type`}"))
              .unless(delivery.`type`.value == MessageDelivery.piuri.value)
            attached <- attachmentAsEncryptedMessage(delivery.attachments.toSeq.flatten.headOption)
            decryptedMessage <- decrypt(attached)
              .provideSomeLayer(ZLayer.succeed(recipientIdentity.agent))
              .mapError(didFailAsThrowable)
            decrypted <- decryptedMessage match
              case plaintext: PlaintextMessage => ZIO.succeed(plaintext)
              case other =>
                ZIO.fail(new RuntimeException(s"Expected plaintext attachment, got ${other.getClass.getSimpleName}"))
            _ <- ZIO.fail(new RuntimeException(s"Expected basicmessage, got ${decrypted.`type`}"))
              .unless(decrypted.`type`.value == "https://didcomm.org/basicmessage/2.0/message")
            _ <- ZIO.fail(new RuntimeException(s"Expected sender ${senderIdentity.shortForm}, got ${decrypted.from}"))
              .unless(decrypted.from.contains(senderIdentity.agent.id.asFROM))
            _ <- ZIO.fail(new RuntimeException(s"Expected recipient ${recipientIdentity.shortForm}, got ${decrypted.to}"))
              .unless(decrypted.to.exists(_.contains(recipientIdentity.agent.id.asTO)))
            content <- ZIO.fromEither(
              decrypted.body.toJson.fromJson[Map[String, String]]
                .flatMap(_.get("content").toRight("Missing body.content"))
            )
          } yield assertTrue(content == "Hello Alice!")
        }
      },
      test("fails DIDComm auth encryption when the recipient PRISM DID has no key-agreement key") {
        ZIO.scoped {
          for {
            _ <- waitForNeoPrism
            senderIdentity <- makeIdentity()
            recipientIdentity <- makeIdentity(PrismIdentityOptions(includeKeyAgreementKey = false))
            _ <- publishAll(senderIdentity, recipientIdentity)
            result <- authEncrypt(plainTextBasicMessage(senderIdentity.shortForm, recipientIdentity.shortForm))
              .provideSomeLayer(ZLayer.succeed(senderIdentity.agent))
              .either
          } yield assertTrue(result.isLeft)
        }
      },
      test("still auth encrypts when the sender PRISM DID has no authentication key") {
        ZIO.scoped {
          for {
            _ <- waitForNeoPrism
            senderIdentity <- makeIdentity(PrismIdentityOptions(includeAuthenticationKey = false))
            recipientIdentity <- makeIdentity()
            _ <- publishAll(senderIdentity, recipientIdentity)
            result <- authEncrypt(plainTextBasicMessage(senderIdentity.shortForm, recipientIdentity.shortForm))
              .provideSomeLayer(ZLayer.succeed(senderIdentity.agent))
              .either
          } yield assertTrue(result.isRight)
        }
      },
      test("returns a DIDComm problem-report when the PRISM resolver is misconfigured") {
        ZIO.scoped {
          for {
            _ <- waitForNeoPrism
            senderIdentity <- makeIdentity()
            legacyMediator = TestAgent(id = AgentStub.mediatorConfig.did, keyStore = AgentStub.mediatorConfig.keyStore)
            _ <- publishAll(senderIdentity)
            _ <- startMediatorServer(legacyMediator, didPrismResolverBaseUrl = "http://127.0.0.1:1/api/dids")
            request = plaintextMediationRequestMessage(senderIdentity.shortForm, legacyMediator.id.string)
            encryptedRequest <- authEncrypt(request)
              .provideSomeLayer(ZLayer.succeed(senderIdentity.agent))
              .mapError(didFailAsThrowable)
            response <- postDidCommMessage(encryptedRequest)
            problemReport: ProblemReport <- response match
              case signed: SignedMessage =>
                ZIO
                  .fromEither(signed.payloadAsPlaintextMessage)
                  .mapError(didFailAsThrowable)
                  .flatMap(pmsg =>
                    ZIO
                      .fromOption(pmsg.toProblemReport.toOption)
                      .orElseFail(new RuntimeException(s"Expected problem-report payload, got ${pmsg.`type`}"))
                  )
              case encrypted: EncryptedMessage =>
                ZIO
                  .fail(new RuntimeException(s"Expected signed fallback problem-report, got ${encrypted.getClass.getSimpleName}"))
              case plaintext: PlaintextMessage =>
                ZIO
                  .fromOption(plaintext.toProblemReport.toOption)
                  .orElseFail(new RuntimeException(s"Expected problem-report payload, got ${plaintext.`type`}"))
            comment <- ZIO.fromOption(problemReport.comment).orElseFail(new RuntimeException("Missing problem-report comment"))
          } yield assertTrue(
            problemReport.piuri == ProblemReport.piuri,
            comment.contains("Unable to resolve DID") || comment.contains("Fail to decrypt Message")
          )
        }
      }
    )
      .provideSomeLayerShared(clientResolverLayer)
      .provideSomeLayerShared(Operations.layerOperations)
      .provideLayerShared(mongoLayer)
      @@ TestAspect.sequential
      @@ TestAspect.withLiveClock
      @@ TestAspect.timeout(2.minutes)

  override def spec =
    if prismE2eEnabled then prismE2eSpec
    else
      suite("MediatorPrismE2ESpec")(
        test("skips PRISM E2E suite unless explicitly enabled") {
          assertTrue(true)
        } @@ TestAspect.ignore
      )

  private def resolverLayer(didPrismResolverBaseUrl: String): ZLayer[Client & Scope, Nothing, Resolver] =
    (
      DidPeerResolver.layerDidPeerResolver ++
        (HttpUtils.layer >>> DIDPrismResolver.layerDIDPrismResolver(didPrismResolverBaseUrl))
    ) >>>
      ZLayer.fromZIO(
        for {
          peer <- ZIO.service[DidPeerResolver]
          prism <- ZIO.service[DIDPrismResolver]
        } yield MultiFallbackResolver(peer, prism): Resolver
      )

  private def makeIdentity(options: PrismIdentityOptions = PrismIdentityOptions()): UIO[PrismIdentity] =
    for {
      seedChunk <- Random.nextBytes(64)
      seed = seedChunk.toArray
      master = HDKey(seed, 0, 0).derive("m/0'/1'/0'")
      masterPrivate = master.getKMMSecp256k1PrivateKey()
      authPair = KMMEdKeyPair.Companion.generateKeyPair()
      authPrivateBytes = authPair.getPrivateKey().getRaw()
      authPublicBytes = authPair.getPublicKey().getRaw()
      agreementPair = KMMX25519KeyPair.Companion.generateKeyPair()
      agreementPrivateBytes = agreementPair.getPrivateKey().getRaw()
      agreementPublicBytes = agreementPair.getPublicKey().getRaw()
      publicKeys = Seq(
        Some(
          compressedPublicKey(
            id = "master-0",
            usage = KeyUsage.MASTER_KEY,
            curve = "secp256k1",
            data = masterPrivate.getPublicKey().getCompressed()
          )
        ),
        Option.when(options.includeAuthenticationKey)(
          compressedPublicKey(
            id = "auth-0",
            usage = KeyUsage.AUTHENTICATION_KEY,
            curve = "Ed25519",
            data = authPublicBytes
          )
        ),
        Option.when(options.includeKeyAgreementKey)(
          compressedPublicKey(
            id = "comm-0",
            usage = KeyUsage.KEY_AGREEMENT_KEY,
            curve = "X25519",
            data = agreementPublicBytes
          )
        )
      ).flatten
      createDid = ProtoCreateDID(
        didData = Some(
          ProtoCreateDID.DIDCreationData(
            publicKeys = publicKeys,
            services = options.serviceEndpoint.toSeq.map(endpoint =>
              Service(
                id = "didcomm-1",
                `type` = "DIDCommMessaging",
                serviceEndpoint = didCommServiceEndpointJson(endpoint)
              )
            )
          )
        )
      )
      operation = PrismOperation(
        operation = PrismOperation.Operation.CreateDid(createDid)
      )
      operationBytes = operation.toByteArray
      shortForm = s"did:prism:${sha256Hex(operationBytes)}"
      longForm = s"$shortForm:${base64UrlNoPad(operationBytes)}"
      signedOperation = SignedPrismOperation(
        signedWith = "master-0",
        signature = ByteString.copyFrom(masterPrivate.sign(operationBytes)),
        operation = Some(operation)
      )
      authKey = Option.when(options.includeAuthenticationKey)(
        OKPPrivateKey(
          kty = KTY.OKP,
          crv = Curve.Ed25519,
          d = base64UrlNoPad(authPrivateBytes),
          x = base64UrlNoPad(authPublicBytes),
          kid = s"$shortForm#auth-0"
        )
      )
      agreementKey = Option.when(options.includeKeyAgreementKey)(
        OKPPrivateKey(
          kty = KTY.OKP,
          crv = Curve.X25519,
          d = base64UrlNoPad(agreementPrivateBytes),
          x = base64UrlNoPad(agreementPublicBytes),
          kid = s"$shortForm#comm-0"
        )
      )
    } yield PrismIdentity(
      shortForm = shortForm,
      longForm = longForm,
      createOperation = operation,
      signedOperation = signedOperation,
      agent = TestAgent(id = DIDSubject(shortForm).toDID, keyStore = KeyStore(Set(authKey, agreementKey).flatten))
    )

  private def compressedPublicKey(id: String, usage: KeyUsage, curve: String, data: Array[Byte]): PublicKey =
    PublicKey(
      id = id,
      usage = usage,
      keyData = PublicKey.KeyData.CompressedEcKeyData(
        CompressedECKeyData(
          curve = curve,
          data = ByteString.copyFrom(data)
        )
      )
    )

  private def didCommServiceEndpointJson(endpoint: String): String =
    s"""{"uri":"$endpoint","accept":["didcomm/v2"]}"""

  private def publishAll(identities: PrismIdentity*): ZIO[Client, Throwable, Unit] =
    submitSignedOperations(identities.map(_.signedOperation)) *>
      ZIO.foreachDiscard(identities)(identity => waitForResolution(identity.shortForm))

  private def submitSignedOperations(operations: Seq[SignedPrismOperation]): ZIO[Client, Throwable, Unit] =
    for {
      url <- ZIO.fromEither(URL.decode(s"$neoprismBaseUrl/api/submissions/signed-operations"))
        .mapError(new RuntimeException(_))
      request = Request
        .post(
          url = url,
          body = Body.fromString(
            SubmitSignedOperationsRequest(
              signed_operations = operations.map(op => hex(op.toByteArray))
            ).toJson
          )
        )
        .addHeader(Header.ContentType(MediaType.application.json))
      response <- Client.batched(request)
      body <- response.body.asString
      _ <- ZIO.fail(new RuntimeException(s"NeoPRISM submission failed: ${response.status.code} $body"))
        .unless(response.status == Status.Ok)
    } yield ()

  private def waitForNeoPrism: ZIO[Client, Throwable, Unit] =
    waitForHttpStatus(s"$neoprismBaseUrl/api/_system/health", Status.Ok, 60.seconds)

  private def waitForResolution(did: String): ZIO[Client, Throwable, Unit] =
    waitForHttpStatus(s"$neoprismBaseUrl/api/dids/$did", Status.Ok, 30.seconds)

  private def assertResolves(did: String): ZIO[Client, Throwable, Unit] =
    resolveDidDocument(did).unit

  private def documentJsonContains(document: DIDDocument, expectedFragments: String*): Boolean = {
    val json = document.toJson
    expectedFragments.forall(json.contains)
  }

  private def resolveDidDocument(did: String): ZIO[Client, Throwable, DIDDocument] =
    for {
      url <- ZIO.fromEither(URL.decode(s"$neoprismBaseUrl/api/dids/$did"))
        .mapError(new RuntimeException(_))
      response <- Client.batched(Request.get(url))
      body <- response.body.asString
      _ <- ZIO.fail(new RuntimeException(s"Failed to resolve DID $did: ${response.status.code} $body"))
        .unless(response.status == Status.Ok)
      document <- ZIO.fromEither(body.fromJson[DIDDocument]).mapError(new RuntimeException(_))
    } yield document

  private def waitForHttpStatus(urlValue: String, expected: Status, timeout: Duration): ZIO[Client, Throwable, Unit] =
    ZIO
      .fromEither(URL.decode(urlValue))
      .mapError(new RuntimeException(_))
      .flatMap(url =>
        Client
          .batched(Request.get(url))
          .flatMap(response =>
            if (response.status == expected) ZIO.unit
            else ZIO.fail(new RuntimeException(s"Unexpected status ${response.status} for $urlValue"))
          )
      )
      .retry(Schedule.spaced(1.second))
      .timeoutFail(new RuntimeException(s"Timed out waiting for $urlValue to become $expected"))(timeout)

  private def startMediatorServer(
      identity: TestAgent,
      didPrismResolverBaseUrl: String = neoprismResolverBaseUrl
  ): ZIO[Client & Scope, Throwable, Unit] = {
    val agentLayer = ZLayer.succeed(MediatorAgent(identity.id, identity.keyStore))
    val httpClient = Scope.default ++ Client.default
    val transportFactory = httpClient >>> TransportFactoryImp.layer
    val resolver = httpClient >>> resolverLayer(didPrismResolverBaseUrl)
    Server
      .serve((MediatorAgent.didCommApp ++ DIDCommRoutes.app) @@ Middleware.cors)
      .provideSomeLayer(resolver)
      .provideSomeLayer(agentLayer)
      .provideSomeLayer(mongoLayer)
      .provideSomeLayer(Scope.default >>> ((agentLayer ++ transportFactory ++ mongoLayer) >>> OperatorImp.layer))
      .provideSomeLayer(Operations.layerOperations)
      .provide(Server.defaultWithPort(mediatorPort))
      .forkScoped
      .unit <*
      waitForHttpStatus(s"$mediatorEndpoint/health", Status.Ok, 30.seconds)
  }

  private def sendExpectReply(sender: TestAgent, plaintextMessage: PlaintextMessage): ZIO[Client & Resolver & Operations, Throwable, PlaintextMessage] =
    for {
      encrypted <- authEncrypt(plaintextMessage)
        .provideSomeLayer(ZLayer.succeed(sender))
        .mapError(didFailAsThrowable)
      response <- postDidComm(encrypted)
      message <- ZIO
        .fromOption(response)
        .orElseFail(new RuntimeException(s"Expected DIDComm reply for ${plaintextMessage.`type`}"))
      decrypted <- decrypt(message)
        .provideSomeLayer(ZLayer.succeed(sender))
        .mapError(didFailAsThrowable)
      plaintext <- decrypted match
        case plaintext: PlaintextMessage => ZIO.succeed(plaintext)
        case other =>
          ZIO.fail(new RuntimeException(s"Expected plaintext reply, got ${other.getClass.getSimpleName}"))
    } yield plaintext

  private def sendWithoutReply(sender: TestAgent, plaintextMessage: PlaintextMessage): ZIO[Client & Resolver & Operations, Throwable, Unit] =
    for {
      encrypted <- authEncrypt(plaintextMessage)
        .provideSomeLayer(ZLayer.succeed(sender))
        .mapError(didFailAsThrowable)
      _ <- postDidComm(encrypted).unit
    } yield ()

  private def postDidComm(message: EncryptedMessage): ZIO[Client, Throwable, Option[EncryptedMessage]] =
    for {
      url <- ZIO.fromEither(URL.decode(mediatorEndpoint)).mapError(new RuntimeException(_))
      request = Request
        .post(url = url, body = Body.fromString(message.toJson))
        .addHeader(MediaTypes.ENCRYPTED.asContentType)
      response <- Client.batched(request)
      body <- response.body.asString
      maybeMessage <-
        if (body.isBlank) ZIO.none
        else
          ZIO
            .fromEither(body.fromJson[Message])
            .mapError(new RuntimeException(_))
            .flatMap {
              case encrypted: EncryptedMessage => ZIO.some(encrypted)
              case other =>
                ZIO.fail(new RuntimeException(s"Expected encrypted DIDComm response, got ${other.getClass.getSimpleName}"))
            }
    } yield maybeMessage

  private def postDidCommMessage(message: EncryptedMessage): ZIO[Client, Throwable, Message] =
    for {
      url <- ZIO.fromEither(URL.decode(mediatorEndpoint)).mapError(new RuntimeException(_))
      request = Request
        .post(url = url, body = Body.fromString(message.toJson))
        .addHeader(MediaTypes.ENCRYPTED.asContentType)
      response <- Client.batched(request)
      body <- response.body.asString
      parsedMessage <-
        if (body.isBlank) ZIO.fail(new RuntimeException("Expected DIDComm response body"))
        else ZIO.fromEither(body.fromJson[Message]).mapError(new RuntimeException(_))
    } yield parsedMessage

  private def attachmentAsEncryptedMessage(attachment: Option[Attachment]): IO[RuntimeException, EncryptedMessage] =
    for {
      attachedMessage <- ZIO
        .fromOption(attachment)
        .orElseFail(new RuntimeException("Missing DIDComm attachment"))
        .flatMap(att => ZIO.fromEither(att.getAsMessage).mapError(new RuntimeException(_)))
      encrypted <- attachedMessage match
        case encrypted: EncryptedMessage => ZIO.succeed(encrypted)
        case other =>
          ZIO.fail(new RuntimeException(s"Expected encrypted attachment, got ${other.getClass.getSimpleName}"))
    } yield encrypted

  private def plaintextMediationRequestMessage(didFrom: String, mediatorDid: String): PlaintextMessage =
    s"""{
       |  "id" : "17f9f122-f762-4ba8-9011-39b9e7efb177",
       |  "type" : "https://didcomm.org/coordinate-mediation/2.0/mediate-request",
       |  "to" : [
       |     "$mediatorDid"
       |  ],
       |  "from" : "$didFrom",
       |  "body" : {},
       |  "return_route" : "all",
       |  "typ" : "application/didcomm-plain+json"
       |}""".stripMargin.fromJson[PlaintextMessage].toOption.get

  private def plaintextKeyListUpdateRequestMessage(didFrom: String, mediatorDid: String, recipientDid: String): PlaintextMessage =
    s"""{
       |  "id" : "cf64e501-d524-4fd9-8314-4dc4bc652983",
       |  "type" : "https://didcomm.org/coordinate-mediation/2.0/keylist-update",
       |  "to" : [
       |     "$mediatorDid"
       |  ],
       |  "from" : "$didFrom",
       |  "body" : {
       |    "updates" : [
       |      {
       |        "recipient_did" : "$recipientDid",
       |        "action" : "add"
       |      }
       |    ]
       |  },
       |  "return_route" : "all",
       |  "typ" : "application/didcomm-plain+json"
       |}""".stripMargin.fromJson[PlaintextMessage].toOption.get

  private def plaintextDeliveryRequestMessage(didFrom: String, mediatorDid: String, recipientDid: String): PlaintextMessage =
    s"""{
       |  "id" : "5d44cc11-d5da-4e19-ba1a-a5279dfea367",
       |  "type" : "https://didcomm.org/messagepickup/3.0/delivery-request",
       |  "to" : [
       |    "$mediatorDid"
       |  ],
       |  "from" : "$didFrom",
       |  "body" : {
       |    "limit" : 5,
       |    "recipient_did" : "$recipientDid"
       |  },
       |  "return_route" : "all",
       |  "typ" : "application/didcomm-plain+json"
       |}""".stripMargin.fromJson[PlaintextMessage].toOption.get

  private def plainTextBasicMessage(didFrom: String, didTo: String): PlaintextMessage =
    s"""{
       |  "id" : "e463a417-7661-4764-b60a-21a3e62ad9cf",
       |  "type" : "https://didcomm.org/basicmessage/2.0/message",
       |  "to" : [
       |    "$didTo"
       |  ],
       |  "from" : "$didFrom",
       |  "body" : {
       |    "content" : "Hello Alice!"
       |  },
       |  "typ" : "application/didcomm-plain+json"
       |}""".stripMargin.fromJson[PlaintextMessage].toOption.get

  private def plaintextForwardMessage(
      didFrom: String,
      forwardTo: String,
      mediatorDid: String,
      attachedMessage: String
  ): PlaintextMessage =
    s"""{
       |  "id" : "f2c8b22f-06ee-4913-b82d-0bc772ade407",
       |  "type" : "https://didcomm.org/routing/2.0/forward",
       |  "to" : [
       |    "$mediatorDid"
       |  ],
       |  "from" : "$didFrom",
       |  "body" : {
       |    "next" : "$forwardTo"
       |  },
       |  "attachments" : [
       |    {
       |      "data" : {
       |        "json" : $attachedMessage
       |      }
       |    }
       |  ],
       |  "typ" : "application/didcomm-plain+json"
       |}""".stripMargin.fromJson[PlaintextMessage].toOption.get

  private def sha256Hex(bytes: Array[Byte]): String =
    hex(MessageDigest.getInstance("SHA-256").digest(bytes))

  private def base64UrlNoPad(bytes: Array[Byte]): String =
    Base64.getUrlEncoder.withoutPadding().encodeToString(bytes)

  private def hex(bytes: Array[Byte]): String =
    bytes.map("%02x".format(_)).mkString

  private def didFailAsThrowable(error: DidFail): Throwable =
    new RuntimeException(error.toString)
}
