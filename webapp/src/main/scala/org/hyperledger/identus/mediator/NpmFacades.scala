package org.hyperledger.identus.mediator

import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@JSImport("qrcode-generator", JSImport.Default)
@js.native
object QRCodeGenerator extends js.Object {
  def apply(typeNumber: Int, errorCorrectionLevel: String): QRCode = js.native
}

@js.native
trait QRCode extends js.Object {
  def addData(data: String): Unit = js.native
  def make(): Unit = js.native
  def createSvgTag(options: js.Object): String = js.native
}

@js.native
trait MDCDrawer extends js.Object {
  var open: Boolean = js.native
}

@JSImport("@material/drawer", "MDCDrawer")
@js.native
object MDCDrawer extends js.Object {
  def attachTo(element: dom.Element): MDCDrawer = js.native
}

@js.native
trait MDCMenu extends js.Object {
  var open: Boolean = js.native
}

@JSImport("@material/menu", "MDCMenu")
@js.native
object MDCMenu extends js.Object {
  def attachTo(element: dom.Element): MDCMenu = js.native
}

@JSImport("@material/ripple", "MDCRipple")
@js.native
object MDCRipple extends js.Object {
  def attachTo(element: dom.Element): Unit = js.native
}
