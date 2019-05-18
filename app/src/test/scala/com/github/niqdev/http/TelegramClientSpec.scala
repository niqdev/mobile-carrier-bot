package com.github.niqdev
package http

import cats.effect.{ IO, Sync }
import com.github.ghik.silencer.silent
import com.github.niqdev.model.{ DatabaseDriver, TelegramSettings, Update }
import io.chrisdavenport.log4cats.Logger
import org.http4s.client.Client
import org.http4s.{ HttpApp, Method, Response, Status }

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

    "verify findLastOffset" in {
      val updates = List(
        Update(5, None),
        Update(1, None),
        Update(4, None),
        Update(8, None),
        Update(3, None),
        Update(6, None)
      )
      val lastOffset = TelegramClient[IO, DatabaseDriver.Cache](settings, logger[IO])
        .findLastOffset(updates)

      lastOffset shouldBe 8
    }

    "verify getUpdates" in {
      import cats.implicits.catsSyntaxApplicativeId
      import org.http4s.Http4sLiteralSyntax

      val client = Client.fromHttpApp(HttpApp[IO] { request =>
        request.method shouldBe Method.GET
        request.uri shouldBe uri"https://api.telegram.org/bot123:xyz/getUpdates?offset=8"

        Response[IO](Status.Ok).pure[IO]
      })

      TelegramClient[IO, DatabaseDriver.Cache](settings, logger[IO])
        .getUpdates(client)(8L)
        .unsafeRunSync()
    }

    /*
    "verify startPolling" in {
      val repository = TelegramRepository[IO, DatabaseDriver.Cache]

      def httpClient[F[_]: Sync]: Client[F] = {
        import cats.implicits.catsSyntaxApplicativeId

        val response = model.Response(
          ok = true,
          result = Some(List(Update(8, None)))
        )

        val app = HttpApp[F] {
          case request if request.pathInfo.contains("getUpdates") =>
            request.params("offset") shouldBe "myOffset"

            Response[F](Status.Ok).withEntity(response).pure[F]
        }
        Client.fromHttpApp(app)
      }

      val messageResponse = TelegramClient[IO, DatabaseDriver.Cache](settings, logger[IO])
        .startPolling(repository, httpClient())
        .take(1)
        .compile
        .toList
        .unsafeRunSync()
        .head

      messageResponse shouldBe "aaa"
    }
   */
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
