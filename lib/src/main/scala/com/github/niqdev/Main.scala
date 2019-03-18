package com.github.niqdev

import java.util.UUID

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.{attr, element, text}
import net.ruippeixotog.scalascraper.dsl.DSL._

object Main extends App {

  private[this] def threeIe(username: String, password: String) = {
    val browser: JsoupBrowser = JsoupBrowser.typed()
    val docGet = browser.get("https://sso.three.ie/mylogin//login?service=https%3A%2F%2Fmy3account.three.ie%2FMy_account_balance")

    val lt = docGet >> attr("value")("input[name=lt]")
    //val cookie = browser.cookies("")("JSESSIONID")

    val form = Map(
      "username" -> username,
      "password" -> password,
      "lt" -> lt
    )

    val docPost = browser.post("https://sso.three.ie/mylogin//login?service=https%3A%2F%2Fmy3account.three.ie%2FMy_account_balance", form)
    val redirectLink = docPost >> attr("href")("a[target=_parent]")
    val redirect = browser.get(redirectLink)
    val balance = redirect >> element("div.P54_myAccountBalance_w1") >> text("td.twenty")
    println(balance)
  }

  private[this] def timIt(username: String, password: String) = {
    val browser: JsoupBrowser = JsoupBrowser.typed()

    val form = Map(
      "username" -> username,
      "password" -> password,
      "urlFwd" -> "https://auth.tim.it/dcareg/public/login/authenticate",
      "tokenLogin" -> s"AREA_PUBBLICA_${UUID.randomUUID}"
    )

    val docLogin = browser.post("https://auth.tim.it/dcauth/faces/dcauth/pages/authentication/genericlogin/genericlogin.xhtml", form)
    //val cookie = browser.cookies("")("DCAUTH_AUTH_COOKIE")
    val docHome = browser.post("https://www.119selfservice.tim.it/area-clienti-119/privata/opzioni", Map())
    val balance = docHome >> element("div.info_user") >> text("p.credito_residuo")
    println(balance)
  }
}
