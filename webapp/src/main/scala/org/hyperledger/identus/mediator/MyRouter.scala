package org.hyperledger.identus.mediator

import com.raquo.laminar.api.L.{*, given}
import com.raquo.waypoint.*
import org.scalajs.dom
import upickle.default.*

object MyRouter {
  sealed abstract class Page(
      val title: String,
      val icon: String // https://fonts.google.com/icons?selected=Material+Icons+Outlined
  )

  case object MediatorPage extends Page("Mediator", "diversity_3")

  given mediatorPageRW: ReadWriter[MediatorPage.type] = macroRW

  given rw: ReadWriter[Page] = macroRW

  private val routes = List(
    // // http://localhost:8080/?_oob=eyJ0eXBlIjoiaHR0cHM6Ly9kaWRjb21tLm9yZy9vdXQtb2YtYmFuZC8yLjAvaW52aXRhdGlvbiIsImlkIjoiNTk5ZjM2MzgtYjU2My00OTM3LTk0ODctZGZlNTUwOTlkOTAwIiwiZnJvbSI6ImRpZDpleGFtcGxlOnZlcmlmaWVyIiwiYm9keSI6eyJnb2FsX2NvZGUiOiJzdHJlYW1saW5lZC12cCIsImFjY2VwdCI6WyJkaWRjb21tL3YyIl19fQ
    Route.static(MediatorPage, root / endOfSegments, Router.localFragmentBasePath),
  )

  val router = new Router[Page](
    routes = routes,
    getPageTitle = _.title, // displayed in the browser tab next to favicon
    serializePage = page => write(page)(using rw), // serialize page data for storage in History API log
    deserializePage = pageStr => read(pageStr)(using rw), // deserialize the above
    // routeFallback = { (_: String) => HomePage },
    routeFallback = { (_: String) => MediatorPage },
  )(
    popStateEvents = windowEvents(_.onPopState), // this is how Waypoint avoids an explicit dependency on Laminar
    owner = unsafeWindowOwner // this router will live as long as the window
  )

  // Note: for fragment ('#') URLs this isn't actually needed.
  // See https://github.com/raquo/Waypoint docs for why this modifier is useful in general.
  def navigateTo(page: Page): Binder[HtmlElement] = Binder { el =>

    val isLinkElement = el.ref.isInstanceOf[dom.html.Anchor]

    if (isLinkElement) {
      el.amend(href(router.absoluteUrlForPage(page)))
    }

    // If element is a link and user is holding a modifier while clicking:
    //  - Do nothing, browser will open the URL in new tab / window / etc. depending on the modifier key
    // Otherwise:
    //  - Perform regular pushState transition
    (onClick
      .filter(ev => !(isLinkElement && (ev.ctrlKey || ev.metaKey || ev.shiftKey || ev.altKey)))
      .preventDefault
      --> (_ => router.pushState(page))).bind(el)
  }
}
