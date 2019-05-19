package com.github.niqdev
package http

import cats.effect.{ IO, Sync }
import com.github.ghik.silencer.silent
import com.github.niqdev.model._
import com.github.niqdev.repository.TelegramRepository
import io.chrisdavenport.log4cats.Logger
import org.http4s.client.Client
import org.http4s.{ HttpApp, Method, Request, Response, Status }

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

  private[this] def verifyHttpRequest[F[_]: Sync](f: Request[F] => Response[F]): Client[F] = {
    import cats.implicits.catsSyntaxApplicativeId

    Client.fromHttpApp(HttpApp[F] { request =>
      f(request).pure[F]
    })
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
      import org.http4s.Http4sLiteralSyntax

      val offset = 8L
      val response = model.Response(
        ok = true,
        result = Some(List(Update(8, None)))
      )
      val client: Client[IO] = verifyHttpRequest[IO] { request =>
        request.method shouldBe Method.GET
        request.uri shouldBe uri"https://api.telegram.org/bot123:xyz/getUpdates?offset=8"
        Response[IO](Status.Ok).withEntity[model.Response[List[Update]]](response)
      }

      val result = TelegramClient[IO, DatabaseDriver.Cache](settings, logger[IO])
        .getUpdates(client)(offset)
        .unsafeRunSync()

      result shouldBe response
    }

    "verify sendMessage" in {
      import org.http4s.Http4sLiteralSyntax

      val sendMessage = SendMessage(3L, "myText")
      val response = model.Response(
        ok = true,
        result = Some(Message(id = 1L, date = 999L))
      )
      val client: Client[IO] = verifyHttpRequest[IO] { request =>
        request.method shouldBe Method.POST
        request.uri shouldBe uri"https://api.telegram.org/bot123:xyz/sendMessage"
        Response[IO](Status.Ok).withEntity(response)
      }

      val result = TelegramClient[IO, DatabaseDriver.Cache](settings, logger[IO])
        .sendMessage(client)(sendMessage)
        .unsafeRunSync()

      result shouldBe response
    }

    "verify startPolling" in {
      val repository = TelegramRepository[IO, DatabaseDriver.Cache]

      val getUpdatesResponse = model.Response(
        ok = true,
        result = Some(
          List(
            Update(
              8,
              Some(
                Message(
                  id = 1L,
                  from = Some(User(1L, false, "test")),
                  date = 1L,
                  text = Some("myValue")
                )
              )
            )
          )
        )
      )

      val sendMessageResponse = model.Response(
        ok = true,
        result = Some(Message(id = 1L, date = 1L))
      )

      def httpClient[F[_]: Sync]: Client[F] = {
        import cats.implicits.catsSyntaxApplicativeId

        val app = HttpApp[F] {
          case request if request.pathInfo.contains("getUpdates") =>
            Response[F](Status.Ok)
              .withEntity(getUpdatesResponse)
              .pure[F]

          case request if request.pathInfo.contains("sendMessage") =>
            Response[F](Status.Ok)
              .withEntity(sendMessageResponse)
              .pure[F]
        }
        Client.fromHttpApp(app)
      }

      val result = TelegramClient[IO, DatabaseDriver.Cache](settings, logger[IO])
        .startPolling(repository, httpClient[IO])
        .take(1)
        .compile
        .toList
        .unsafeRunSync()
        .head

      result shouldBe sendMessageResponse
      repository.getOffset.unsafeRunSync() shouldBe 9
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
