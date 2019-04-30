package com.github.niqdev
package http

import cats.effect.{ ConcurrentEffect, Resource, Timer }
import cats.syntax.functor.toFunctorOps
import com.github.ghik.silencer.silent
import com.github.niqdev.model.{ Response, TelegramSettings, Update }
import com.github.niqdev.repository.TelegramRepository
import fs2.Stream
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.client.Client

import scala.concurrent.duration.{ DurationLong, FiniteDuration }

sealed abstract class TelegramClient[F[_]: ConcurrentEffect: Timer, D](
  settings: TelegramSettings,
  log: Logger[F]
) {

  private[http] def logStream[T]: T => Stream[F, T] =
    (t: T) => Stream.eval(log.debug(s"$t").map(_ => t))

  private[http] def getUpdates(client: Client[F]): FiniteDuration => Stream[F, Response[Vector[Update]]] =
    _ =>
      Stream.eval {
        client.expect[Response[Vector[Update]]](s"${settings.apiUri}/getUpdates")
      }

  @silent
  def startPolling(
    repository: TelegramRepository[F, D],
    client: Client[F]
  ) =
    Stream
      .awakeEvery[F](settings.polling.value.seconds)
      .flatMap(getUpdates(client))
      .flatMap(logStream[Response[Vector[Update]]])
      .holdOptionResource
}

object TelegramClient {

  private[http] def apply[F[_]: ConcurrentEffect: Timer, D](
    settings: TelegramSettings,
    log: Logger[F]
  ): TelegramClient[F, D] =
    new TelegramClient[F, D](settings, log) {}

  def startPolling[F[_]: ConcurrentEffect: Timer, D](
    settings: TelegramSettings,
    repository: TelegramRepository[F, D],
    client: Client[F]
  ) =
    for {
      log    <- Resource.liftF(Slf4jLogger.create[F])
      client <- apply(settings, log).startPolling(repository, client)
    } yield client
}
