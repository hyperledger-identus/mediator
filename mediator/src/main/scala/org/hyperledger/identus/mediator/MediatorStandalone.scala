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
    did: DID,
    keyStore: KeyStore
) {
  val agentLayer: ZLayer[Any, Nothing, MediatorAgent] =
    ZLayer(MediatorAgent.make(id = did, keyStore = keyStore))
}

object MediatorConfig {

  def legacy(
      endpoints: String,
      keyAgreement: OKPPrivateKeyWithoutKid,
      keyAuthentication: OKPPrivateKeyWithoutKid
  ): MediatorConfig = {
    val agent = DIDPeer2.makeAgent(
      Seq(keyAgreement, keyAuthentication),
      endpoints
        .split(";")
        .toSeq
        .map { endpoint => fmgp.util.Base64.encode(s"""{"t":"dm","s":{"uri":"$endpoint","a":["didcomm/v2"]}}""") }
        .map(DIDPeerServiceEncodedNew(_))
    )
    MediatorConfig(did = agent.id, keyStore = agent.keyStore)
  }

  private val didConfig =
    Config
      .string("did")
      .mapOrFail(str =>
        DIDSubject.either(str) match
          case Left(value)  => Left(Config.Error.InvalidData(Chunk("did"), "Fail to parse the DID: " + value.error))
          case Right(value) => Right(value.toDID: DID)
      )

  private val keyStoreConfig =
    Config
      .Sequence(
        Config.Fallback[PrivateKeyWithKid](Config.derived[OKPPrivateKeyWithKid], Config.derived[ECPrivateKeyWithKid])
      )
      .map(keys => KeyStore(keys.toSet))
      .nested("keyStore")

  private val explicitConfig =
    (didConfig ++ keyStoreConfig).map((did, keyStore) => MediatorConfig(did = did, keyStore = keyStore))

  private val legacyConfig =
    (
      Config.string("endpoints") ++
        Config.derived[OKPPrivateKeyWithoutKid].nested("keyAgreement") ++
        Config.derived[OKPPrivateKeyWithoutKid].nested("keyAuthentication")
    ).map((endpoints, keyAgreement, keyAuthentication) => legacy(endpoints, keyAgreement, keyAuthentication))

  val config: Config[MediatorConfig] =
    Config.Fallback(explicitConfig, legacyConfig)
}

case class DataBaseConfig(
    connectionString: Option[String],
    protocol: String,
    host: String,
    port: Option[String],
    userName: String,
    password: String,
    dbName: String
) {
  private def maybePort = port.filter(_.nonEmpty).map(":" + _).getOrElse("")
  private def buildConnectionString = s"$protocol://$userName:$password@$host$maybePort/$dbName"
  
  // Use provided connection string if available, otherwise construct from components
  val finalConnectionString = connectionString.getOrElse(buildConnectionString)
  
  // Display connection string with masked password
  val displayConnectionString = connectionString match {
    case Some(connStr) => connStr.replaceAll("://[^:]*:[^@]*@", "://***:***@") // Mask credentials in URI
    case None => s"$protocol://$userName:******@$host$maybePort/$dbName"
  }
  
  override def toString: String = s"""DataBaseConfig($connectionString, $protocol, $host, $port, $userName, "******", $dbName)"""
}

object MediatorStandalone extends ZIOAppDefault {
  val mediatorColorFormat: LogFormat =
    fiberId.color(LogColor.YELLOW) |-|
      line.highlight |-|
      allAnnotations |-|
      cause.highlight

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j(mediatorColorFormat)

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
    mediatorConfig <- configs.nested("identity").nested("mediator").load(MediatorConfig.config)
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
    didPrismResolverBaseUrl <- configs
      .nested("mediator")
      .load(Config.string("didPrismResolver"))
    _ <- ZIO.log(s"DID PRISM resolver: $didPrismResolverBaseUrl")
    httpClient = Scope.default ++ Client.default
    transportFactory = httpClient >>> TransportFactoryImp.layer
    resolver = httpClient >>> resolverLayer(didPrismResolverBaseUrl)
    mongo = AsyncDriverResource.layer >>> ReactiveMongoApi.layer(mediatorDbConfig.finalConnectionString)
    repos = mongo >>> (MessageItemRepo.layer ++ UserAccountRepo.layer ++ OutboxMessageRepo.layer)
    myServer <- Server
      .serve((MediatorAgent.didCommApp ++ DIDCommRoutes.app) @@ (Middleware.cors))
      .provideSomeLayer(resolver)
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
