package com.github.niqdev
package algebra

import java.util.UUID

import cats.effect.Sync
import com.github.niqdev.model.MobileNetworkOperator.{ThreeIe, TimIt}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.{attr, element, text}
import net.ruippeixotog.scalascraper.dsl.DSL._

// TODO Credentials, Balance, refined
trait MobileCarrierClient[F[_], MNO] {
  def balance(username: String, password: String): F[String]
}

object MobileCarrierClient extends MobileCarrierClientInstances {
  def apply[F[_], MNO](implicit C: MobileCarrierClient[F, MNO]): MobileCarrierClient[F, MNO] = C
}

trait MobileCarrierClientInstances {

  implicit def threeMobileCarrierClient[F[_]: Sync]: MobileCarrierClient[F, ThreeIe] =
    (username: String, password: String) =>
      Sync[F].delay {
        val url =
          "https://sso.three.ie/mylogin//login?service=https%3A%2F%2Fmy3account.three.ie%2FMy_account_balance"
        val browser: JsoupBrowser = JsoupBrowser.typed()
        val docGet = browser.get(url)

        val lt = docGet >> attr("value")("input[name=lt]")
        //val cookie = browser.cookies("")("JSESSIONID")

        val form = Map(
          "username" -> username,
          "password" -> password,
          "lt" -> lt
        )

        val docPost = browser.post(url, form)
        val redirectLink = docPost >> attr("href")("a[target=_parent]")
        val redirect = browser.get(redirectLink)
        val balance = redirect >> element("div.P54_myAccountBalance_w1") >> text("td.twenty")
        balance
      }

  implicit def timMobileCarrierClient[F[_]: Sync]: MobileCarrierClient[F, TimIt] =
    (username: String, password: String) =>
      Sync[F].delay {
        val browser: JsoupBrowser = JsoupBrowser.typed()

        val form = Map(
          "username" -> username,
          "password" -> password,
          "urlFwd" -> "https://auth.tim.it/dcareg/public/login/authenticate",
          "tokenLogin" -> s"AREA_PUBBLICA_${UUID.randomUUID}"
        )

        browser.post(
          "https://auth.tim.it/dcauth/faces/dcauth/pages/authentication/genericlogin/genericlogin.xhtml",
          form
        )
        //val cookie = browser.cookies("")("DCAUTH_AUTH_COOKIE")
        val docHome =
          browser.post("https://www.119selfservice.tim.it/area-clienti-119/privata/opzioni", Map())
        val balance = docHome >> element("div.info_user") >> text("p.credito_residuo")
        balance
      }

}
