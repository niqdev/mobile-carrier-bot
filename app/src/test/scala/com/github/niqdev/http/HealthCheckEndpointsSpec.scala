package com.github.niqdev
package http

import java.nio.charset.StandardCharsets

import cats.effect.IO
import com.github.niqdev.model.{BuildInformation, Settings}
import com.github.niqdev.service.HealthCheckService
import org.http4s.{Method, Request, Status, Uri}

final class HealthCheckEndpointsSpec extends BaseSpec {

  "HealthCheckEndpoints" must {

    "verify statusEndpoint" in {
      val request = Request[IO](method = Method.GET, uri = Uri.unsafeFromString("/status"))
      val maybeResponse = HealthCheckEndpoints[IO]
        .statusEndpoint
        .run(request)
        .value
        .unsafeRunSync()

      maybeResponse match {
        case Some(response) =>
          response.status shouldBe Status.Ok

          val byteArray = response.body.compile.toVector.unsafeRunSync().toArray
          new String(byteArray, StandardCharsets.UTF_8) shouldBe Status.Ok.reason
        case _ =>
          fail("unexpected failure")
      }
    }

    "verify infoEndpoint" in {
      val request = Request[IO](method = Method.GET, uri = Uri.unsafeFromString("/info"))
      val maybeResponse = HealthCheckEndpoints[IO]
        .infoEndpoint(HealthCheckService())
        .run(request)
        .value
        .unsafeRunSync()

      maybeResponse match {
        case Some(response) =>
          response.status shouldBe Status.Ok

          response.attemptAs[BuildInformation].value.unsafeRunSync() match {
            case Right(buildInformation) =>
              buildInformation.name shouldBe "mobile-carrier-bot"
              buildInformation.scalaVersion shouldBe "2.12.8"

              buildInformation.version shouldBe "0.1"
              // e.g. 2019-05-15 18:17:03.286
              buildInformation.buildTime.nonEmpty shouldBe true
            case _ =>
              fail("unexpected failure")
          }
        case _ =>
          fail("unexpected failure")
      }
    }

    "verify envEndpoint" in {
      val settings = Settings.load[IO].allocated.unsafeRunSync()._1
      val request = Request[IO](method = Method.GET, uri = Uri.unsafeFromString("/env"))
      val maybeResponse = HealthCheckEndpoints[IO]
        .envEndpoint(settings)
        .run(request)
        .value
        .unsafeRunSync()

      maybeResponse match {
        case Some(response) =>
          response.status shouldBe Status.Ok

          response.as[Settings].unsafeRunSync() shouldBe settings
        case _ =>
          fail("unexpected failure")
      }
    }
  }

}
