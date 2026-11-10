package org.hyperledger.identus.mediator

import scala.scalajs.js.annotation.JSExportTopLevel
import scala.scalajs.js.annotation.JSExport

import org.scalajs.dom
import com.raquo.laminar.codecs._
import com.raquo.laminar.api.L._

import MyRouter._

@JSExportTopLevel("AppUtils")
object AppUtils {

  def onEnterPress = onKeyPress.filter(_.keyCode == dom.KeyCode.Enter)

  val menuClickObserver = Observer[dom.MouseEvent](onNext = ev => {
    val tmp = MDCDrawer.attachTo(dom.window.document.querySelector(".mdc-drawer"))
    tmp.open_=(!tmp.open)
  })

  val optionsClickObserver = Observer[dom.MouseEvent](onNext = ev => {
    val tmp = MDCMenu.attachTo(dom.window.document.querySelector(".mdc-menu"))
    tmp.open_=(!tmp.open)
  })

  def topBarHeader(title: Signal[String]) = { // (title: String) = {
    val menuButton = button(
      className("material-icons mdc-top-app-bar__navigation-icon mdc-icon-button"),
      aria.label("Options"),
      onClick --> menuClickObserver,
      "menu"
    )

    MDCRipple.attachTo(menuButton.ref)

    val options = {
      div(
        className("mdc-menu mdc-menu-surface"),
        minWidth("200px"),
        ul(
          className("mdc-list"),
          role("menu"),
          aria.hidden(true),
          aria.orientation("vertical"),
          tabIndex(-1),
          li(className("mdc-list-divider"), role("separator")),
          li(
            ul(
              className("mdc-menu__selection-group"),
              // Global.dids.map { did => makeLi(did, "person_outline") }
            )
          ),
        )
      )
    }

    val optionsButton = button(
      className("material-icons mdc-top-app-bar__navigation-icon mdc-icon-button"),
      aria.label("Open navigation menu"),
      onClick --> optionsClickObserver,
      "more_vert"
    )
    MDCMenu.attachTo(options.ref)

    headerTag(
      div(
        className("mdc-top-app-bar__row"),
        sectionTag(
          className("mdc-top-app-bar__section mdc-top-app-bar__section--align-start"),
          menuButton,
          span(className("mdc-top-app-bar__title"), child.text <-- title)
        ),
        /* TODO REMOVE
        sectionTag(
          className("mdc-top-app-bar__section mdc-top-app-bar__section--align-start"),
          div(
            a(
              href := "https://github.com/hyperledger/identus",
              target := "_blank",
              img(
                // src := "https://example.com/example.svg",
                src := "public/example.svg", // Note: this is not the best server for CDN
                className := "logo vanilla",
                alt := "Identus Mediator"
              ),
            )
          )
        ),
         */
        sectionTag(
          className("mdc-top-app-bar__section mdc-top-app-bar__section--align-end"),
          role("toolbar"),
          div(
            className("mdc-menu-surface--anchor"),
            optionsButton,
            options
          )
        ),
      ),
    )
  }

  val drawerScrim = div(className("mdc-drawer-scrim"))
  def drawer(linkPages: List[Page], currentPage: Signal[Page]) =
    asideTag(
      className("mdc-drawer mdc-drawer--modal"),
      div(
        className("mdc-drawer__header"),
        h3(className("mdc-drawer__title"), "Hyperledger - Identus Mediator"),
        h6(className("mdc-drawer__subtitle"), "atala@iohk.io"),
      ),
      div(
        className("mdc-drawer__content"),
        navTag(
          className("mdc-list"),
          linkPages.map(page =>
            a(
              className <-- currentPage.map { p =>
                if (p == page) "mdc-list-item mdc-list-item--activated" else "mdc-list-item"
              },
              aria.current := "page",
              tabIndex(0),
              span(className("mdc-list-item__ripple")),
              i(
                className("material-icons mdc-list-item__graphic"),
                aria.hidden(true),
                page.icon
              ),
              navigateTo(page),
              span(className("mdc-list-item__text"), page.title),
            ),
          )
        )
      )
    )

  def myButton(text: String) = {
    div(
      className("mdc-touch-target-wrapper"),
      button(
        className("mdc-button mdc-button--touch mdc-button--raised"),
        span(className("mdc-button__ripple")),
        span(className("mdc-button__touch")),
        span(className("mdc-button__label"), text),
      )
    )

  }

}
