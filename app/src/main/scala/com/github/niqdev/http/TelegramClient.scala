package com.github.niqdev
package http

import cats.effect.{ ConcurrentEffect, Resource, Sync, Timer }
import cats.syntax.functor.toFunctorOps
import com.github.niqdev.model._
import com.github.niqdev.model.telegram.BotCommand
import com.github.niqdev.repository.TelegramRepository
import fs2.{ Pipe, Stream }
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.client.Client
import org.http4s.{ Method, Request, Uri }

import scala.concurrent.duration.DurationLong

sealed abstract class TelegramClient[F[_]: ConcurrentEffect: Timer, D](
  settings: TelegramSettings,
  log: Logger[F]
) {

  private[http] def logDebug[T]: T => F[Unit] =
    (t: T) => log.debug(s"$t")

  private[http] def buildPath(path: String): Uri =
    Uri
      .unsafeFromString(s"${settings.baseUri}")
      .withPath(s"/bot${settings.apiToken.value}$path")

  private[http] def findLastOffset(updates: List[Update]): Long =
    updates.max.id

  /**
    * [[https://core.telegram.org/bots/api#getupdates getUpdates]]
    */
  private[http] def getUpdates(client: Client[F]): Long => F[Response[List[Update]]] =
    offset => {
      val uri = buildPath("/getUpdates")
        .withQueryParam("offset", offset)

      client.expect[Response[List[Update]]](uri)
    }

  /**
    * [[https://core.telegram.org/bots/api#sendmessage sendMessage]]
    */
  private[http] def sendMessage(client: Client[F]): SendMessage => F[Response[Message]] =
    message => {
      val request = Request[F](
        Method.POST,
        buildPath("/sendMessage")
      ).withEntity(message)

      client.expect[Response[Message]](request)
    }

  private[http] def collectUpdates: Pipe[F, Response[List[Update]], (Long, List[Update])] =
    (updatesStream: Stream[F, Response[List[Update]]]) =>
      updatesStream
        .map {
          case Response(true, _, Some(updates), None, None) if updates.nonEmpty =>
            Right(findLastOffset(updates) -> updates)
          case response @ Response(true, _, Some(updates), _, _) if updates.isEmpty =>
            Left(s"empty Response[List[Update]]: $response")
          case response =>
            Left(s"invalid Response[List[Update]]: $response")
        }
        .evalTap {
          case Right(response) =>
            log.debug(s"new Response[List[Update]]: [offset=${response._1}][size=${response._2.size}]")
          case Left(response) if response.startsWith("empty") =>
            log.debug(response)
          case Left(response) =>
            log.error(response)
        }
        .collect {
          // filter valid updates
          case Right(result) => result
        }

  private[http] def collectMessage: Pipe[F, Update, SendMessage] =
    updateStream =>
      updateStream
        .map {
          case Update(_, Some(Message(_, Some(user), _, Some(text)))) =>
            // TODO
            Right(
              SendMessage(
                user.id,
                BotCommand.parseCommand(text)
              )
            )
          case update =>
            Left(s"invalid Update: $update")
        }
        .evalTap {
          case Right(_) =>
            Sync[F].unit
          case Left(result) =>
            log.warn(result)
        }
        .collect {
          // filter valid message
          case Right(result) => result
        }

  private[http] def startPolling(
    repository: TelegramRepository[F, D],
    client: Client[F]
  ): Stream[F, Response[Message]] =
    Stream
      .awakeEvery[F](settings.polling.value.seconds)
      .evalMap(_ => repository.getOffset)
      .evalMap(getUpdates(client))
      .through(collectUpdates)
      // offset should be updated after processing or only after a successful response?
      // what if there is a huge backlog and the service was done for long time?
      // in this case a user would receive responses to obsolete requests
      .evalMap {
        // TODO move in service
        case (lastOffset, updates) =>
          // an update is considered confirmed as soon as getUpdates
          // is called with an offset higher than its update_id
          repository.setOffset(lastOffset + 1).map(_ => updates)
      }
      // flatten: Stream[F, Seq[T]] ==> Stream[F, T]
      .flatMap(Stream.emits)
      .evalTap(logDebug[Update])
      .through(collectMessage)
      .evalMap(sendMessage(client))
      .evalTap(logDebug[Response[Message]])

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
      log <- Resource.liftF(Slf4jLogger.create[F])
      client <- apply(settings, log)
        .startPolling(repository, client)
        .holdOptionResource
    } yield client
}
