package org.hyperledger.identus.mediator

import fmgp.crypto.*
import fmgp.crypto.error.*
import fmgp.did.*
import fmgp.did.comm.*
import fmgp.did.comm.protocol.*
import fmgp.did.method.peer.*
import fmgp.did.framework.TransportFactoryImp
import org.hyperledger.identus.mediator.db.*
import org.hyperledger.identus.mediator.protocols.*
import zio.*
import zio.stream.*
import zio.config.*
import zio.config.magnolia.*
import zio.config.typesafe.*
import zio.http.*
import zio.json.*
import zio.logging.*
import zio.logging.LogFormat.*
import zio.logging.backend.SLF4J

import java.time.format.DateTimeFormatter
import scala.io.Source
case class MediatorConfig(endpoints: String, keyAgreement: OKPPrivateKey, keyAuthentication: OKPPrivateKey) {
  val did = DIDPeer2.makeAgent(
    Seq(keyAgreement, keyAuthentication),
    endpoints
      .split(";")
      .toSeq
      .map { endpoint => fmgp.util.Base64.encode(s"""{"t":"dm","s":{"uri":"$endpoint","a":["didcomm/v2"]}}""") }
      .map(DIDPeerServiceEncodedNew(_))
  )
  val agentLayer: ZLayer[Any, Nothing, MediatorAgent] =
    ZLayer(MediatorAgent.make(id = did.id, keyStore = did.keyStore))
}
case class DataBaseConfig(
    protocol: String,
    host: String,
    port: Option[String],
    userName: String,
    password: String,
    dbName: String
) {
  private def maybePort = port.filter(_.nonEmpty).map(":" + _).getOrElse("")
  val connectionString = s"$protocol://$userName:$password@$host$maybePort/$dbName"
  val displayConnectionString = s"$protocol://$userName:******@$host$maybePort/$dbName"
  override def toString: String = s"""DataBaseConfig($protocol, $host, $port, $userName, "******", $dbName)"""
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
        |Vist: https://github.com/hyperledger/identus-mediator""".stripMargin
    )
    configs = ConfigProvider.fromResourcePath()
    mediatorConfig <- configs.nested("identity").nested("mediator").load(deriveConfig[MediatorConfig])
    agentLayer = mediatorConfig.agentLayer
    _ <- ZIO.log(s"Identus Mediator APP. See https://github.com/hyperledger/identus-mediator")
    _ <- ZIO.log(s"MediatorConfig: $mediatorConfig")
    _ <- ZIO.log(s"DID: ${mediatorConfig.did.id.string}")
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
    transportFactory = Scope.default >>> (Client.default >>> TransportFactoryImp.layer)
    mongo = AsyncDriverResource.layer >>> ReactiveMongoApi.layer(mediatorDbConfig.connectionString)
    repos = mongo >>> (MessageItemRepo.layer ++ UserAccountRepo.layer ++ OutboxMessageRepo.layer)
    myServer <- Server
      .serve((MediatorAgent.didCommApp ++ DIDCommRoutes.app) @@ (Middleware.cors))
      .provideSomeLayer(DidPeerResolver.layerDidPeerResolver)
      .provideSomeLayer(agentLayer)
      .provideSomeLayer(repos)
      .provideSomeLayer((agentLayer ++ transportFactory ++ repos ++ Scope.default) >>> OperatorImp.layer)
      .provideSomeLayer(Operations.layerDefault)
      .provideSomeLayer(Scope.default)
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
