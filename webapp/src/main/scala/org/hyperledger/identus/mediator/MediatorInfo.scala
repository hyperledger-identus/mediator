package org.hyperledger.identus.mediator

import com.raquo.laminar.api.L.*
import fmgp.did.*
import fmgp.did.comm.*
import fmgp.did.comm.protocol.oobinvitation.OOBInvitation
import org.scalajs.dom
import typings.qrcodeGenerator
import typings.qrcodeGenerator.anon.CellSize
import zio.json.*

import scala.scalajs.js.Date

@scala.scalajs.js.annotation.JSExportTopLevel("MediatorInfo")
object MediatorInfo {
  val divHealthCheck = div()
  @scala.scalajs.js.annotation.JSExport
  def healthCheck = HttpClient.runProgram {
    HttpClient.healthCheck
      .map(e =>
        e.status match
          case 200 =>
            divHealthCheck.ref.replaceChildren(
              div("Mediator status:", code("ok")).ref,
              div(s"Status update at ${Date()}").ref,
            )
          case 503 =>
            divHealthCheck.ref.replaceChildren(
              div(styleAttr := "color:red;", "Status Warning:", code(e.body)).ref,
              div(s"Mediator status update at ${Date()}").ref
            )
          case code =>
            divHealthCheck.ref.replaceChildren(
              div(s"Mediator with unexpected status code '$code' in health check").ref,
              div(s"Status update at ${Date()}").ref
            )
      )
  }

  val invitation = OOBInvitation(
    from = Global.mediatorDID,
    goal_code = Some("request-mediate"),
    goal = Some("RequestMediate"),
    accept = Some(Seq("didcomm/v2")),
  )
  val host = dom.window.location.host
  val scheme = dom.window.location.protocol
  val fullPath = s"${scheme}//${host}"
  val qrCodeData = OutOfBand.from(invitation.toPlaintextMessage).makeURI(s"$fullPath")

  val divQRCode = div(width := "55%")
  {
    val aux = qrcodeGenerator.mod.^.apply(qrcodeGenerator.TypeNumber.`0`, qrcodeGenerator.ErrorCorrectionLevel.L)
    aux.addData(qrCodeData)
    aux.make()
    val cellSize = CellSize().setScalable(true)
    divQRCode.ref.innerHTML = aux.createSvgTag(cellSize)
  }

  def apply(): HtmlElement = // rootElement
    div(
      onMountCallback(ctx => healthCheck),
      h1("Invite for the DID Comm Mediator:"),
      div(
        h3("Mediator identity (DID):"),
        code(invitation.from.value),
      ),
      div(
        h3("Mediator Health Check:"),
        divHealthCheck,
      ),
      h3("Plaintext out of band invitation:"),
      p(a(href := fullPath, target := "_blank", code(qrCodeData))), // FIXME make it a link to the mobile app
      pre(code(invitation.toPlaintextMessage.toJsonPretty)),
      p(
        "To facilitate the integration with other systems you can get the plain text invitation and the out-of-band invitation on the following endpoints:",
        " '/invitation' and '/invitationOOB'.",
        "You can also get the DID of the mediator in '/did' or the build version (of the backend) in '/version'."
      ),
      divQRCode,
      h3("Signed out of band invitation:"),
      code("TODO"),
      footerTag(
        textAlign.center,
        p("Mediator Version: ", code("'" + MediatorBuildInfo.version + "'"))
      ),
    )

}
