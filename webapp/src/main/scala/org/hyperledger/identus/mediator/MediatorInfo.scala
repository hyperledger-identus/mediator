package org.hyperledger.identus.mediator

import org.scalajs.dom
import com.raquo.laminar.api.L.*
import typings.qrcodeGenerator
import zio.json.*
import fmgp.did.*
import fmgp.did.comm.*
import fmgp.did.comm.protocol.oobinvitation.OOBInvitation
import typings.qrcodeGenerator.anon.CellSize

object MediatorInfo {

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
      h1("Invite for the DID Comm Mediator:"),
      div(
        h3("Mediator identity (DID):"),
        code(invitation.from.value),
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
