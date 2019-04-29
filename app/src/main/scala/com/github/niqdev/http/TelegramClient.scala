package com.github.niqdev
package http

import cats.effect.{ ConcurrentEffect, Resource, Sync, Timer }
import com.github.niqdev.model.{ Response, TelegramSettings, Update }
import fs2.Stream
import fs2.concurrent.Signal
import org.http4s.client.Client

import scala.concurrent.duration.DurationLong

sealed abstract class TelegramClient {

  def startPolling[F[_]: ConcurrentEffect: Timer](
    client: Client[F],
    settings: TelegramSettings
  ): Resource[F, Signal[F, Option[Unit]]] =
    Stream
      .awakeEvery[F](settings.polling.value.seconds)
      .flatMap(
        _ =>
          Stream.eval {
            client.expect[Response[Vector[Update]]](s"${settings.uri}/getUpdates")
          }
      )
      .flatMap(response => Stream.eval(Sync[F].delay(println(response))))
      .holdOptionResource
}

object TelegramClient {

  def apply(): TelegramClient =
    new TelegramClient {}

  def startPolling[F[_]: ConcurrentEffect: Timer](
    client: Client[F],
    settings: TelegramSettings
  ): Resource[F, Signal[F, Option[Unit]]] =
    apply().startPolling(client, settings)
}
