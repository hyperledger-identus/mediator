package org.hyperledger.identus.mediator

import scala.scalajs.js.annotation._

import scalajs.js
import org.scalajs.dom
import com.raquo.laminar.api.L._
import com.raquo.waypoint._

import MyRouter._
import com.raquo.airstream.ownership.ManualOwner

import org.scalajs.dom.ServiceWorkerRegistration
import scala.scalajs.js.JSON
object App {

  def main( /*args: Array[String]*/ ): Unit = {

    // This div, its id and contents are defined in index-fastopt.html and index-fullopt.html files
    lazy val container = dom.document.getElementById("app-container")

    lazy val appElement = {
      div(
        AppUtils.drawer(linkPages, MyRouter.router.currentPageSignal),
        AppUtils.drawerScrim,
        AppUtils.topBarHeader(
          MyRouter.router.currentPageSignal.map { case p: MediatorPage.type =>
            "Identus Mediator"
          }
        ),
        mainTag(
          className("mdc-top-app-bar--fixed-adjust"),
          child <-- selectedApp.signal
        )
      )
    }

    // Wait until the DOM is loaded, otherwise app-container element might not exist
    renderOnDomContentLoaded(container, appElement)

  }

  private val selectedApp = SplitRender(MyRouter.router.currentPageSignal)
    .collectStatic(MediatorPage)(MediatorInfo())

  private val linkPages: List[Page] = List(
    MediatorPage,
  )

}
