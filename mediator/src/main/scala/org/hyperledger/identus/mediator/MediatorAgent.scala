package org.hyperledger.identus.mediator

import fmgp.crypto.*
import fmgp.crypto.error.*
import fmgp.did.*
import fmgp.did.comm.*
import fmgp.did.comm.protocol.*
import fmgp.did.comm.protocol.oobinvitation.OOBInvitation
import fmgp.did.comm.protocol.reportproblem2.ProblemReport
import io.netty.handler.codec.http.HttpHeaderNames
import org.hyperledger.identus.mediator.*
import org.hyperledger.identus.mediator.db.*
import org.hyperledger.identus.mediator.protocols.*
import reactivemongo.api.bson.{*, given}
import zio.*
import zio.http.*
import zio.http.*
import zio.http.Header.AccessControlAllowMethods
import zio.http.Header.AccessControlAllowOrigin
import zio.http.Header.HeaderType
import zio.json.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source
import scala.util.Try

case class MediatorAgent(
    override val id: DID,
    override val keyStore: KeyStore, // Should we make it lazy with ZIO
) extends Agent { def keys: Seq[PrivateKey] = keyStore.keys.toSeq }

object MediatorAgent {

  def didCommApp =
    didCommAppAux @@ TraceIdMiddleware.addTraceId

  def make(id: DID, keyStore: KeyStore): ZIO[Any, Nothing, MediatorAgent] = ZIO.succeed(MediatorAgent(id, keyStore))

  def didCommAppAux = {
    Routes(
      Method.GET / "headers" -> handler { (req: Request) =>
        val data = req.headers.toSeq.map(e => (e.headerName, e.renderedValue))
        ZIO.succeed(Response.text("HEADERS:\n" + data.mkString("\n") + "\nRemoteAddress:" + req.remoteAddress)).debug
      },
      Method.GET / "health" -> handler { (req: Request) =>
        for {
          userStatsFiber <- ZIO
            .service[UserAccountRepo]
            .flatMap(_.stats)
            .timeout(5.seconds)
            .either
            .map {
              case Right(Some(value)) => Right(s"Ok: $value")
              case Right(None)        => Left(s"Error: timeout")
              case Left(error)        => Left(s"Error: $error")
            }
            .fork
          messageStatsFiber <- ZIO
            .service[MessageItemRepo]
            .flatMap(_.stats)
            .timeout(5.seconds)
            .either
            .map {
              case Right(Some(value)) => Right(s"Ok: $value")
              case Right(None)        => Left(s"Error: timeout")
              case Left(error)        => Left(s"Error: $error")
            }
            .fork
          outboxStatsFiber <-
            ZIO
              .service[OutboxMessageRepo]
              .flatMap(_.stats)
              .timeout(5.seconds)
              .either
              .map {
                case Right(Some(value)) => Right(s"Ok: $value")
                case Right(None)        => Left(s"Error: timeout")
                case Left(error)        => Left(s"Error: $error")
              }
              .fork
          userStats <- userStatsFiber.join
          messageStats <- messageStatsFiber.join
          outboxStats <- outboxStatsFiber.join
          services = Seq(userStats, messageStats, outboxStats)
          ret <-
            if (services.exists(_.isLeft)) {
              // Response.serviceUnavailable( //This method does not work - BUG in ZIO http ?
              ZIO.logWarning("health check fail!") *>
                ZIO.succeed(
                  Response(
                    Status.ServiceUnavailable,
                    Headers(Header.ContentType(MediaType.text.plain).untyped),
                    Body.fromCharSequence(
                      s"""Unable to connect to the Database collections:
                     |UserAccountRepo=${userStats.merge}
                     |MessageItemRepo=${messageStats.merge}
                     |OutboxMessageRepo=${outboxStats.merge}
                     |""".stripMargin.trim()
                    )
                  )
                )
            } else ZIO.succeed(Response.ok)
        } yield ret
      },
      Method.GET / "version" -> handler { (req: Request) => ZIO.succeed(Response.text(MediatorBuildInfo.version)) },
      Method.GET / "did" -> handler { (req: Request) =>
        for {
          agent <- ZIO.service[MediatorAgent]
          ret <- ZIO.succeed(Response.text(agent.id.string))
        } yield (ret)
      },
      Method.GET / "invitation" -> handler { (req: Request) =>
        for {
          agent <- ZIO.service[MediatorAgent]
          annotationMap <- ZIO.logAnnotations.map(_.map(e => LogAnnotation(e._1, e._2)).toSeq)
          invitation = OOBInvitation(
            from = agent.id,
            goal_code = Some("request-mediate"),
            goal = Some("RequestMediate"),
            accept = Some(Seq("didcomm/v2")),
          )
          _ <- ZIO.log("New mediate invitation MsgID: " + invitation.id.value)
          ret <- ZIO.succeed(Response.json(invitation.toPlaintextMessage.toJson))
        } yield (ret)
      },
      Method.GET / "invitationOOB" -> handler { (req: Request) =>
        for {
          agent <- ZIO.service[MediatorAgent]
          annotationMap <- ZIO.logAnnotations.map(_.map(e => LogAnnotation(e._1, e._2)).toSeq)
          invitation = OOBInvitation(
            from = agent.id,
            goal_code = Some("request-mediate"),
            goal = Some("RequestMediate"),
            accept = Some(Seq("didcomm/v2")),
          )
          _ <- ZIO.log("New mediate invitation MsgID: " + invitation.id.value)
          ret <- ZIO.succeed(
            Response.text(
              OutOfBand.from(invitation.toPlaintextMessage).makeURI("")
            )
          )
        } yield (ret)
      },
      Method.GET / trailing -> handler { (req: Request) =>
        for {
          agent <- ZIO.service[MediatorAgent]
          _ <- ZIO.log("index.html")
          ret <- ZIO.succeed(IndexHtml.html(agent.id))
        } yield ret
      },
      Method.GET / "public" / string("path") -> handler { (path: String, req: Request) =>
        // RoutesMiddleware
        // TODO https://zio.dev/reference/stream/zpipeline/#:~:text=ZPipeline.gzip%20%E2%80%94%20The%20gzip%20pipeline%20compresses%20a%20stream%20of%20bytes%20as%20using%20gzip%20method%3A
        val fullPath = s"public/$path"
        val classLoader = Thread.currentThread().getContextClassLoader()
        val headerContentType = fullPath match
          case s if s.endsWith(".html") => Header.ContentType(MediaType.text.html)
          case s if s.endsWith(".js")   => Header.ContentType(MediaType.text.javascript)
          case s if s.endsWith(".css")  => Header.ContentType(MediaType.text.css)
          case s if s.endsWith(".svg")  => Header.ContentType(MediaType.image.`svg+xml`)
          case s                        => Header.ContentType(MediaType.text.plain)
        Handler.fromResource(fullPath).map(_.addHeader(headerContentType))
      }.flatten
    )
  }.sandbox

}
