package com.github.niqdev.service

import cats.effect.{ ContextShift, IO, Timer }
import org.http4s.client.blaze.BlazeClientBuilder

object TelegramService {

  import scala.concurrent.ExecutionContext.global

  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  implicit val timer: Timer[IO]     = IO.timer(global)

  def updates: IO[String] =
    BlazeClientBuilder[IO](global).resource.use { client =>
      val target = "https://http4s.org/v0.18/client/"
      client.expect[String](target)
    }

}
