package com.github.niqdev
package http

import cats.effect._
import com.github.niqdev.model.TelegramSettings
import fs2.Stream
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationLong

sealed abstract class TelegramClient[F[_]: ConcurrentEffect: Timer] {

  private[http] def httpClient(executionContext: ExecutionContext): Stream[F, Client[F]] =
    BlazeClientBuilder[F](executionContext).stream

  private[http] def getUpdates(client: Client[F], settings: TelegramSettings): Stream[F, Unit] =
    Stream
      .awakeEvery[F](settings.polling.value.seconds)
      .flatMap(_ => Stream.eval(client.expect[String](s"${settings.uri}/getMe")))
      .flatMap(response => Stream.eval(Sync[F].delay(println(response))))

  def startPolling(settings: TelegramSettings, executionContext: ExecutionContext): Stream[F, Unit] =
    for {
      client <- httpClient(executionContext)
      _      <- getUpdates(client, settings)
    } yield ()
}

object TelegramClient {

  def apply[F[_]: ConcurrentEffect: Timer]: TelegramClient[F] = new TelegramClient[F] {}
}
