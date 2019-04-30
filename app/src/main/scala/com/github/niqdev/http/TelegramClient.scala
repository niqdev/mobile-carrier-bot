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

import scala.concurrent.duration.DurationLong

sealed abstract class TelegramClient[F[_]: ConcurrentEffect: Timer, D](
  settings: TelegramSettings,
  log: Logger[F]
) {

  private[http] def logStream[T]: T => F[T] =
    (t: T) => log.debug(s"$t").map(_ => t)

  /**
    * [[https://core.telegram.org/bots/api#getupdates getUpdates]]
    */
  private[http] def getUpdates(client: Client[F]): Long => F[Response[Vector[Update]]] =
    offset => client.expect[Response[Vector[Update]]](s"${settings.apiUri}/getUpdates?offset=$offset")

  private[http] def findLastOffset(updates: Vector[Update]): Long =
    updates.max.id

  @silent
  def startPolling(
    repository: TelegramRepository[F, D],
    client: Client[F]
  ) =
    Stream
      .awakeEvery[F](settings.polling.value.seconds)
      .evalMap(_ => repository.getOffset)
      .evalMap(getUpdates(client))
      .evalMap(logStream[Response[Vector[Update]]])
      .collect {
        case Response(true, _, Some(updates), _, _) if updates.nonEmpty =>
          (findLastOffset(updates), updates)
      }
      .evalMap {
        case (lastOffset, updates) =>
          // an update is considered confirmed as soon as getUpdates
          // is called with an offset higher than its update_id
          repository.setOffset(lastOffset + 1).map(_ => updates)
      }
      // flatten
      .flatMap(Stream.emits)
      .evalMap(logStream[Update])
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
