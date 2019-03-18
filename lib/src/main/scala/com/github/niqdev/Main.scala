package com.github.niqdev

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.{attr, element, text}
import net.ruippeixotog.scalascraper.dsl.DSL._

object Main extends App {

  val browser: JsoupBrowser = JsoupBrowser.typed()
  val doc = browser.parseFile("html/login.html")
  val docGet = browser.get("https://sso.three.ie/mylogin//login?service=https%3A%2F%2Fmy3account.three.ie%2FMy_account_balance")

  val lt = docGet >> attr("value")("input[name=lt]")
  val jSessionId = browser.cookies("")("JSESSIONID")

  val form = Map(
    "username" -> "",
    "password" -> "",
    "lt" -> lt
  )

  val docPost = browser.post("https://sso.three.ie/mylogin//login?service=https%3A%2F%2Fmy3account.three.ie%2FMy_account_balance", form)
  val redirectLink = docPost >> attr("href")("a[target=_parent]")
  val redirect = browser.get(redirectLink)
  val balance = redirect >> element("div.P54_myAccountBalance_w1") >> text("td.twenty")
  println(balance)

}
