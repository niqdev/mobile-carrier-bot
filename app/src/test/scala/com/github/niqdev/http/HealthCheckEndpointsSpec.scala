package com.github.niqdev
package http

import java.nio.charset.StandardCharsets

import cats.effect.IO
import org.http4s.{Method, Request, Status, Uri}

// https://github.com/jaspervz/todo-http4s-doobie/blob/master/src/test/scala/service/TodoServiceSpec.scala
// https://github.com/jaspervz/todo-http4s-doobie/blob/master/src/it/scala/TodoServerSpec.scala
// https://github.com/barambani/http4s-extend/blob/master/src/main/scala/http4s/extend/syntax/ResponseVerificationSyntax.scala#L40
final class HealthCheckEndpointsSpec extends BaseSpec {

  "HealthCheckEndpoints" must {

    "verify statusEndpoint" in {
      val request = Request[IO](method = Method.GET, uri = Uri.unsafeFromString("/status"))
      val maybeResponse = HealthCheckEndpoints[IO].statusEndpoint.run(request).value.unsafeRunSync()

      maybeResponse match {
        case Some(response) =>
          response.status shouldBe Status.Ok

          val byteArray = response.body.compile.toVector.unsafeRunSync().toArray
          new String(byteArray, StandardCharsets.UTF_8) shouldBe Status.Ok.reason
        case _ =>
          fail("unexpected failure")
      }
    }
  }

}
