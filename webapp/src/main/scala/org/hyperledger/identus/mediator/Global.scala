package org.hyperledger.identus.mediator

import com.raquo.laminar.api.L.*
import fmgp.did.*
import fmgp.did.comm.*
import fmgp.did.comm.TO
import fmgp.did.method.peer.DIDPeer
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

object Global {

  var mediatorDID = {
    val didSTR = dom.document.querySelector("""meta[name="did"]""")
    FROM(didSTR.getAttribute("content"))
  }

  def clipboardSideEffect(text: => String): Any => Unit =
    (_: Any) => { dom.window.navigator.clipboard.writeText(text) }

}
