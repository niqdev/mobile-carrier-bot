package com.github.niqdev.service

import java.util.concurrent.TimeUnit

import cats.effect.{ IO, Sync }
import com.github.niqdev.model.Settings
import org.http4s.client.Client

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

sealed abstract class TelegramService {

  def poll[F[_]: Sync](httpClient: Client[F], settings: Settings, ec: ExecutionContext): IO[Unit] =
    IO.timer(ec)
      .sleep(FiniteDuration(settings.telegramPolling, TimeUnit.SECONDS))
      .flatMap { _ =>
        IO.delay {
          println("Hello!")
        }
      }
      .flatMap(_ => poll(httpClient, settings, ec))
}

object TelegramService {

  def apply: TelegramService =
    new TelegramService {}

}
