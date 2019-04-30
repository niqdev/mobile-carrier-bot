package com.github.niqdev
package http

import cats.effect.{ ConcurrentEffect, Resource, Timer }
import com.github.ghik.silencer.silent
import com.github.niqdev.model.{ Response, TelegramSettings, Update }
import com.github.niqdev.repository.TelegramRepository
import fs2.Stream
import fs2.concurrent.Signal
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.client.Client

import scala.concurrent.duration.DurationLong

sealed abstract class TelegramClient {

  @silent
  def startPolling[F[_]: ConcurrentEffect: Timer, D](
    settings: TelegramSettings,
    repository: TelegramRepository[F, D],
    client: Client[F]
  )(
    log: Logger[F]
  ): Resource[F, Signal[F, Option[Unit]]] =
    Stream
      .awakeEvery[F](settings.polling.value.seconds)
      .flatMap(
        _ =>
          Stream.eval {
            client.expect[Response[Vector[Update]]](s"${settings.uri}/getUpdates")
          }
      )
      .flatMap(response => Stream.eval(log.debug(s"$response")))
      .holdOptionResource
}

object TelegramClient {

  private[http] def apply: TelegramClient =
    new TelegramClient {}

  def startPolling[F[_]: ConcurrentEffect: Timer, D](
    settings: TelegramSettings,
    repository: TelegramRepository[F, D],
    client: Client[F]
  ): Resource[F, Signal[F, Option[Unit]]] =
    for {
      log    <- Resource.liftF(Slf4jLogger.create[F])
      client <- apply.startPolling(settings, repository, client)(log)
    } yield client
}
