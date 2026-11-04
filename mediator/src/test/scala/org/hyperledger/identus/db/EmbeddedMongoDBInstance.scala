package org.hyperledger.identus.mediator.db
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.transitions.{ImmutableMongod, Mongod, RunningMongodProcess}
import de.flapdoodle.embed.process.io.ProcessOutput
import de.flapdoodle.reverse.TransitionWalker
import de.flapdoodle.reverse.transitions.Start
import zio.{Task, ZIO, ZLayer}
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.api.bson.BSONDocument
object EmbeddedMongoDBInstance {
  val port = 27777
  val hostIp = "localhost"
  val connectionString = s"mongodb://$hostIp:$port/messages"

  private val platformOverrideProperty = "de.flapdoodle.os.override"

  /** flapdoodle detects the host platform to pick the mongod package to download, and its OS detection only knows
    * Ubuntu releases up to 25.10. On any newer release (for example the ubuntu-26.04 runner image) the lookup fails
    * with `could not resolve package for V6_0`. Pin the distribution mongod 6.0 is published for (ubuntu2204) via
    * flapdoodle's documented override, unless the caller already provided one.
    */
  private def pinMongodPlatform(): Unit = {
    val architecture = sys.props.get("os.arch").getOrElse("") match {
      case "aarch64" | "arm64" => "ARM_64"
      case _ => "X86_64"
    }
    val isMissingOverride = sys.props.get(platformOverrideProperty).forall(_.isBlank)
    if (scala.util.Properties.isLinux && isMissingOverride) {
      System.setProperty(platformOverrideProperty, s"Linux|$architecture|Ubuntu|Ubuntu_22_04")
    }
  }

  def layer(
      port: Int = 27077,
      hostIp: String = "localhost"
  ): ZLayer[Any, Throwable, TransitionWalker.ReachedState[RunningMongodProcess]] = {
    pinMongodPlatform()
    ZLayer.scoped(
      ZIO.acquireRelease(
        ZIO.attemptBlocking {
          Mongod
            .builder()
            .processOutput(Start.to(classOf[ProcessOutput]).initializedWith(ProcessOutput.silent()))
            .net(Start.to(classOf[Net]).initializedWith(Net.of(hostIp, port, false)))
            .build()
            .start(Version.Main.V6_0)
        }
      )(process => ZIO.succeed(process.close()))
    )
  }

}
