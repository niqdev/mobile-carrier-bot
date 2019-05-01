package com.github.niqdev
package http

import cats.effect.{ ConcurrentEffect, Resource, Timer }
import cats.syntax.functor.toFunctorOps
import com.github.niqdev.model._
import com.github.niqdev.repository.TelegramRepository
import fs2.Stream
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.client.Client
import org.http4s.{ Method, Request, Uri }

import scala.concurrent.duration.DurationLong

sealed abstract class TelegramClient[F[_]: ConcurrentEffect: Timer, D](
  settings: TelegramSettings,
  log: Logger[F]
) {

  // TODO evalTap
  private[http] def logStream[T]: T => F[T] =
    (t: T) => log.debug(s"$t").map(_ => t)

  // FIXME exception during macro expansion: uri"${settings.baseUri}"
  // import org.http4s.Http4sLiteralSyntax
  private[http] def buildPath(path: String): Uri =
    Uri
      .unsafeFromString(s"${settings.baseUri}")
      .withPath(s"/bot${settings.apiToken.value}$path")

  /**
    * [[https://core.telegram.org/bots/api#getupdates getUpdates]]
    */
  private[http] def getUpdates(client: Client[F]): Long => F[Response[Vector[Update]]] =
    offset =>
      client
        .expect[Response[Vector[Update]]](buildPath("/getUpdates").withQueryParam("offset", offset))

  private[http] def findLastOffset(updates: Vector[Update]): Long =
    updates.max.id

  /**
    * [[https://core.telegram.org/bots/api#sendmessage SendMessage]]
    */
  private[http] def sendMessage(client: Client[F]): SendMessage => F[Response[Message]] =
    message => {
      val request = Request[F](
        Method.POST,
        buildPath("/sendMessage")
      ).withEntity(message)

      client.expect[Response[Message]](request)
    }

  def startPolling(
    repository: TelegramRepository[F, D],
    client: Client[F]
  ) =
    Stream
      .awakeEvery[F](settings.polling.value.seconds)
      .evalMap(_ => repository.getOffset)
      .evalMap(getUpdates(client))
      .evalMap(logStream[Response[Vector[Update]]])
      // TODO remove collect and log errors e.g. attempt.observeEither
      .collect {
        // ignore invalid response
        case Response(true, _, Some(updates), _, _) if updates.nonEmpty =>
          (findLastOffset(updates), updates)
      }
      .evalMap {
        // TODO move in service
        case (lastOffset, updates) =>
          // an update is considered confirmed as soon as getUpdates
          // is called with an offset higher than its update_id
          repository.setOffset(lastOffset + 1).map(_ => updates)
      }
      // flatten: Stream[F, Seq[T]] ==> Stream[F, T]
      .flatMap(Stream.emits)
      .evalMap(logStream[Update])
      .collect {
        // ignore invalid message
        case Update(_, Some(Message(_, Some(user), _, Some(text)))) =>
          SendMessage(
            user.id,
            BotCommand.parseCommand(text)
          )
      }
      .evalMap(sendMessage(client))
      .evalMap(logStream[Response[Message]])
      .holdOptionResource
}

/**
  * [[https://core.telegram.org/bots/api#available-methods]]
  */
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
