package com.github.niqdev.service

import cats.effect.{ ConcurrentEffect, Sync, Timer }
import com.github.niqdev.model.Settings
import fs2.Stream
//import org.http4s.Http4sLiteralSyntax
import org.http4s.client.Client

sealed abstract class TelegramService[F[_]: ConcurrentEffect: Timer] {

//  def buildUri(settings: Settings, method: String): Uri =
//    uri"https://api.telegram.org/bot${settings.telegramApiToken}".withPath(s"/$method")

  import scala.concurrent.duration.DurationLong

  def pollingGetUpdates(client: Client[F], settings: Settings): Stream[F, Unit] =
    Stream
      .awakeEvery[F](settings.telegramPolling.seconds)
      .flatMap(_ => Stream.eval(client.expect[String](s"https://api.telegram.org/bot${settings.telegramApiToken}/getMe")))
      .flatMap(response => Stream.eval(Sync[F].delay(println(response))))

}

object TelegramService {

  def apply[F[_]: ConcurrentEffect: Timer]: TelegramService[F] = new TelegramService[F] {}
}
