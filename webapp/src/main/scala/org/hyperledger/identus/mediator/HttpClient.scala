package org.hyperledger.identus.mediator

import scala.util.chaining.scalaUtilChainingOps
import scala.scalajs.js
import org.scalajs.dom._

import zio._
import zio.json._

import fmgp.did.comm.EncryptedMessage

import fmgp._

case class HealthResponse(status: Int, body: String)

@scala.scalajs.js.annotation.JSExportTopLevel("HttpClient")
object HttpClient {

  @scala.scalajs.js.annotation.JSExport
  val urlHost = window.location.protocol + "//" + window.location.host

  @scala.scalajs.js.annotation.JSExport
  def healthCheck: UIO[HealthResponse] = ZIO
    .fromPromiseJS(
      fetch("/health", new RequestInit { method = HttpMethod.GET })
    )
    .flatMap(r => ZIO.fromPromiseJS(r.text()).map(body => HealthResponse(r.status, body)))
    .catchAll(ex => ZIO.succeed(HealthResponse(-1, ex.getMessage())))

  @scala.scalajs.js.annotation.JSExport
  def runProgram[E](program: ZIO[Any, E, Unit]) = Unsafe.unsafe { implicit unsafe => // Run side effect
    Runtime.default.unsafe.fork(
      program
        .catchAllCause(cause => ZIO.logErrorCause("runProgram Fail", cause))
        .catchAllDefect(error => ZIO.logError("runProgram with Defect: " + error.getMessage()))
    )
  }

}
