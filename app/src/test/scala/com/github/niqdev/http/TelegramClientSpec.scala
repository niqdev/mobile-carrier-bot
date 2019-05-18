package com.github.niqdev
package http

import cats.effect.{ IO, Sync }
import com.github.ghik.silencer.silent
import com.github.niqdev.model.{ DatabaseDriver, TelegramSettings }
import io.chrisdavenport.log4cats.Logger

import scala.concurrent.ExecutionContext

final class TelegramClientSpec extends BaseSpec {

  private[this] val ec             = ExecutionContext.global
  private[this] val cs             = IO.contextShift(ec)
  private[this] implicit val ce    = IO.ioConcurrentEffect(cs)
  private[this] implicit val timer = IO.timer(ec)

  private[this] def settings = {
    import eu.timepit.refined.auto.autoRefineV

    TelegramSettings(
      apiToken = "123:xyz",
      polling = 2L
    )
  }
  private[this] def logger[F[_]: Sync] =
    new DebugLogger[F]() {
      override def debug(message: =>String): F[Unit] =
        Sync[F].unit
    }

  "TelegramClient" must {

    "verify logStream" in {
      def verifyDebugLogger[F[_]: Sync](expected: String) =
        new DebugLogger[F]() {
          override def debug(message: =>String): F[Unit] =
            Sync[F].defer {
              expected shouldBe message
              Sync[F].unit
            }
        }

      val message = "myDebugLog"

      TelegramClient[IO, DatabaseDriver.Cache](settings, verifyDebugLogger(message))
        .logStream(message)
        .unsafeRunSync()
    }

    "verify buildPath" in {
      import org.http4s.Http4sLiteralSyntax

      val path = "/myPath"
      val uri = TelegramClient[IO, DatabaseDriver.Cache](settings, logger[IO])
        .buildPath(path)

      uri shouldBe uri"https://api.telegram.org/bot123:xyz/myPath"
    }
  }
}

@silent
abstract class DebugLogger[F[_]](implicit F: Sync[F]) extends Logger[F] {

  override def error(message: =>String): F[Unit] =
    F.raiseError(???)

  override def warn(message: =>String): F[Unit] =
    F.raiseError(???)

  override def info(message: =>String): F[Unit] =
    F.raiseError(???)

  override def debug(message: =>String): F[Unit]

  override def trace(message: =>String): F[Unit] =
    F.raiseError(???)

  override def error(t: Throwable)(message: =>String): F[Unit] =
    F.raiseError(???)

  override def warn(t: Throwable)(message: =>String): F[Unit] =
    F.raiseError(???)

  override def info(t: Throwable)(message: =>String): F[Unit] =
    F.raiseError(???)

  override def debug(t: Throwable)(message: =>String): F[Unit] =
    F.raiseError(???)

  override def trace(t: Throwable)(message: =>String): F[Unit] =
    F.raiseError(???)
}
