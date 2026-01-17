package org.hyperledger.identus.mediator

import fmgp.crypto.*
import fmgp.crypto.OKPPrivateKey.*
import fmgp.crypto.OKPPrivateKeyWithKid.*
import fmgp.crypto.error.*
import fmgp.did.*
import fmgp.did.comm.*
import fmgp.did.comm.protocol.*
import fmgp.did.framework.TransportFactoryImp
import fmgp.did.method.peer.*
import fmgp.did.method.prism.*
import org.hyperledger.identus.mediator.db.*
import org.hyperledger.identus.mediator.protocols.*
import zio.*
import zio.config.*
import zio.config.magnolia.*
import zio.config.typesafe.*
import zio.http.*
import zio.json.*
import zio.logging.*
import zio.logging.LogFormat.*
import zio.logging.backend.SLF4J
import zio.stream.*

import java.time.format.DateTimeFormatter
import scala.io.Source

object CurveConfig:

  // Config for OKPCurve union type
  given okpCurveConfig: Config[OKPCurve] =
    Config.string.mapOrFail:
      case "X25519"  => Right(Curve.X25519)
      case "Ed25519" => Right(Curve.Ed25519)
      case other => Left(Config.Error.InvalidData(message = s"Invalid OKP curve: $other. Expected X25519 or Ed25519"))

  // Config for ECCurve union type
  given ecCurveConfig: Config[ECCurve] =
    Config.string.mapOrFail:
      case "P-256"     => Right(Curve.`P-256`)
      case "P-384"     => Right(Curve.`P-384`)
      case "P-521"     => Right(Curve.`P-521`)
      case "secp256k1" => Right(Curve.secp256k1)
      case other =>
        Left(
          Config.Error.InvalidData(message = s"Invalid EC curve: $other. Expected P-256, P-384, P-521, or secp256k1")
        )

import CurveConfig.given

case class MediatorConfig(
    did: DIDSubject,
    keyStore: KeyStore
) {
  val agentLayer: ZLayer[Any, Nothing, MediatorAgent] =
    ZLayer(MediatorAgent.make(id = did, keyStore = keyStore))
}
object MediatorConfig {
  // val configPrivateKeyWithKid: Config[PrivateKeyWithKid] = { // hack to drop the nested name
  //   val tmp = Config.derived[PrivateKeyWithKid] // ECPrivateKeyWithKid OKPPrivateKeyWithKid
  //   tmp
  //     .asInstanceOf[Config.Fallback[PrivateKeyWithKid]]
  //     .first
  //     .asInstanceOf[Config.Lazy[PrivateKeyWithKid]]
  //     .thunk()
  //     .asInstanceOf[Config.Nested[PrivateKeyWithKid]]
  //     .config
  // }

  val config = {
    Config
      .string("did")
      .mapOrFail(str =>
        DIDSubject.either(str) match
          case Left(value)  => Left(Config.Error.InvalidData(Chunk("did"), "Fail to parse the DID: " + value.error))
          case Right(value) => Right(value)
      ) ++
      Config
        .Sequence(
          Config.Fallback[PrivateKeyWithKid](Config.derived[OKPPrivateKeyWithKid], Config.derived[ECPrivateKeyWithKid])
        )
        .map(keys => KeyStore(keys.toSet))
        .nested("keyStore")
  }.map((did, keyStore) => MediatorConfig(did = did, keyStore = keyStore))
}

case class DataBaseConfig(
    connectionString: String,
) {
  val finalConnectionString = connectionString

  // Display connection string with masked password
  val displayConnectionString = connectionString.replaceAll("://[^:]*:[^@]*@", "://***:***@")

  override def toString: String =
    s"""DataBaseConfig($displayConnectionString)"""
}

object MediatorStandalone extends ZIOAppDefault {
  val mediatorColorFormat: LogFormat =
    fiberId.color(LogColor.YELLOW) |-|
      line.highlight |-|
      allAnnotations |-|
      cause.highlight

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j(mediatorColorFormat)

  def mainProgram = for {
    _ <- Console.printLine( // https://patorjk.com/software/taag/#p=display&f=ANSI%20Shadow&t=Mediator
      """███╗   ███╗███████╗██████╗ ██╗ █████╗ ████████╗ ██████╗ ██████╗ 
        |████╗ ████║██╔════╝██╔══██╗██║██╔══██╗╚══██╔══╝██╔═══██╗██╔══██╗
        |██╔████╔██║█████╗  ██║  ██║██║███████║   ██║   ██║   ██║██████╔╝
        |██║╚██╔╝██║██╔══╝  ██║  ██║██║██╔══██║   ██║   ██║   ██║██╔══██╗
        |██║ ╚═╝ ██║███████╗██████╔╝██║██║  ██║   ██║   ╚██████╔╝██║  ██║
        |╚═╝     ╚═╝╚══════╝╚═════╝ ╚═╝╚═╝  ╚═╝   ╚═╝    ╚═════╝ ╚═╝  ╚═╝
        |Yet another server simpler Mediator server DID Comm v2.
        |Visit: https://github.com/hyperledger-identus/mediator""".stripMargin
    )
    configs = ConfigProvider.fromResourcePath()
    mediatorConfig <- configs
      .nested("identity")
      .nested("mediator")
      .load(MediatorConfig.config) // deriveConfig[MediatorConfig]
    agentLayer = mediatorConfig.agentLayer
    _ <- ZIO.log(s"Identus Mediator APP. See https://github.com/hyperledger-identus/mediator")
    _ <- ZIO.log(s"MediatorConfig: $mediatorConfig")
    _ <- ZIO.log(s"DID: ${mediatorConfig.did.string}")
    mediatorDbConfig <- configs.nested("database").nested("mediator").load(deriveConfig[DataBaseConfig])
    _ <- ZIO.log(s"MediatorDb Connection String: ${mediatorDbConfig.displayConnectionString}")
    port <- configs
      .nested("http")
      .nested("server")
      .nested("mediator")
      .load(Config.int("port"))
    _ <- ZIO.log(s"Starting server on port: $port")
    escalateTo <- configs
      .nested("report")
      .nested("problem")
      .nested("mediator")
      .load(Config.string("escalateTo"))
    _ <- ZIO.log(s"Problem reports escalated to: $escalateTo")
    httpClient = Scope.default ++ Client.default
    transportFactory = httpClient >>> TransportFactoryImp.layer
    mongo = AsyncDriverResource.layer >>> ReactiveMongoApi.layer(mediatorDbConfig.finalConnectionString)
    repos = mongo >>> (MessageItemRepo.layer ++ UserAccountRepo.layer ++ OutboxMessageRepo.layer)
    baseUrlForDIDPrismResolverVar <- configs
      .nested("mediator")
      .load(Config.string("didPrismResolver"))
    didResolver = for {
      peer <- DidPeerResolver.layerDidPeerResolver
      prism <- DIDPrismResolver.layerDIDPrismResolver(baseUrlForDIDPrismResolverVar)
    } yield ZEnvironment(MultiFallbackResolver(peer.get, prism.get): Resolver) // MultiParResolver(peer, prism)
    myServer <- Server
      .serve((MediatorAgent.didCommApp ++ DIDCommRoutes.app) @@ (Middleware.cors))
      .provideSomeLayer(httpClient >>> HttpUtils.layer >>> didResolver)
      .provideSomeLayer(agentLayer)
      .provideSomeLayer(repos)
      .provideSomeLayer(Scope.default >>> ((agentLayer ++ transportFactory ++ repos) >>> OperatorImp.layer))
      .provideSomeLayer(Operations.layerOperations)
      .provide(Server.defaultWithPort(port))
      .debug
      .fork
    _ <- ZIO.log(s"Mediator Started")
    _ <- myServer.join *> ZIO.log(s"Mediator End")
    _ <- ZIO.log(s"*" * 100)
  } yield ()

  override val run = ZIO.logAnnotate(
    zio.LogAnnotation("version", MediatorBuildInfo.version)
  ) { mainProgram }

}
