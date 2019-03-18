package com.github.niqdev

import net.ruippeixotog.scalascraper.browser.JsoupBrowser

object Main extends App {

  val browser = JsoupBrowser()
  val doc = browser.parseFile("html/login.html")
  println(doc)

}
