package com.github.niqdev
package http

import cats.effect.{IO, Sync}
import com.github.niqdev.model._
import com.github.niqdev.repository.TelegramRepository
import fs2.Stream
import org.http4s.client.Client
import org.http4s.{HttpApp, Method, Request, Response, Status}

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
    new TestLogger[F]() {}

  private[this] def verifyHttpRequest[F[_]: Sync](f: Request[F] => Response[F]): Client[F] = {
    import cats.implicits.catsSyntaxApplicativeId

    Client.fromHttpApp(HttpApp[F] { request =>
      f(request).pure[F]
    })
  }

  "TelegramClient" must {

    "verify logDebug" in {
      def verifyLogger[F[_]: Sync](expected: String) =
        new TestLogger[F]() {
          override def debug(message: =>String): F[Unit] =
            Sync[F].defer {
              message shouldBe expected
              Sync[F].unit
            }
        }

      val message = "myDebugLog"

      TelegramClient[IO, DatabaseDriver.Cache](settings, verifyLogger(message))
        .logDebug(message)
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

    "verify collectUpdates: valid response" in {

      val expectedUpdates = List(
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
      val updatesResponse = model.Response(
        ok = true,
        result = Some(expectedUpdates)
      )

      def verifyLogger[F[_]: Sync] =
        new TestLogger[F]() {
          override def debug(message: =>String): F[Unit] =
            Sync[F].defer {
              message shouldBe "new Response[List[Update]]: [offset=8][size=1]"
              Sync[F].unit
            }
        }

      val (offset, updates) = TelegramClient[IO, DatabaseDriver.Cache](settings, verifyLogger[IO])
        .collectUpdates(Stream.eval(IO(updatesResponse)))
        .take(1)
        .compile
        .toList
        .unsafeRunSync()
        .head

      offset shouldBe 8
      updates shouldBe expectedUpdates
    }

    "verify collectUpdates: empty response" in {
      val updatesResponse = model.Response(
        ok = true,
        result = Some(List.empty[Update])
      )

      def verifyLogger[F[_]: Sync] =
        new TestLogger[F]() {
          override def debug(message: =>String): F[Unit] =
            Sync[F].defer {
              message shouldBe "empty Response[List[Update]]: Response(true,None,Some(List()),None,None)"
              Sync[F].unit
            }
        }

      val result: List[(Long, List[Update])] =
        TelegramClient[IO, DatabaseDriver.Cache](settings, verifyLogger[IO])
          .collectUpdates(Stream.eval(IO(updatesResponse)))
          .take(1)
          .compile
          .toList
          .unsafeRunSync()

      result.isEmpty shouldBe true
    }

    "verify collectUpdates: invalid response" in {
      val updatesResponse = model.Response[List[Update]](
        ok = false,
        description = Some("myDescription"),
        errorCode = Some(1L),
        parameters = Some(ResponseParameters(2L, 3L))
      )

      def verifyLogger[F[_]: Sync] =
        new TestLogger[F]() {
          override def error(message: =>String): F[Unit] =
            Sync[F].defer {
              message shouldBe "invalid Response[List[Update]]: Response(false,Some(myDescription),None,Some(1),Some(ResponseParameters(2,3)))"
              Sync[F].unit
            }
        }

      val result: List[(Long, List[Update])] =
        TelegramClient[IO, DatabaseDriver.Cache](settings, verifyLogger[IO])
          .collectUpdates(Stream.eval(IO(updatesResponse)))
          .take(1)
          .compile
          .toList
          .unsafeRunSync()

      result.isEmpty shouldBe true
    }

    "verify collectMessage: valid message" in {
      val update = Update(
        8,
        Some(
          Message(
            id = 1L,
            from = Some(User(2L, false, "test")),
            date = 3L,
            text = Some(BotCommand.Start.entryName)
          )
        )
      )

      def verifyLogger[F[_]: Sync] =
        new TestLogger[F]() {
          override def warn(message: =>String): F[Unit] =
            Sync[F].defer {
              message shouldBe "invalid Update"
              Sync[F].unit
            }
        }

      val result = TelegramClient[IO, DatabaseDriver.Cache](settings, verifyLogger[IO])
        .collectMessage(Stream.eval(IO(update)))
        .take(1)
        .compile
        .toList
        .unsafeRunSync()
        .head

      result shouldBe SendMessage(2L, "TODO start")
    }

    "verify collectMessage: invalid message" in {
      val update = Update(
        8,
        Some(
          Message(
            id = 1L,
            from = None,
            date = 3L,
            text = None
          )
        )
      )

      def verifyLogger[F[_]: Sync] =
        new TestLogger[F]() {
          override def warn(message: =>String): F[Unit] =
            Sync[F].defer {
              message shouldBe "invalid Update: Update(8,Some(Message(1,None,3,None)))"
              Sync[F].unit
            }
        }

      val result = TelegramClient[IO, DatabaseDriver.Cache](settings, verifyLogger[IO])
        .collectMessage(Stream.eval(IO(update)))
        .take(1)
        .compile
        .toList
        .unsafeRunSync()

      result.isEmpty shouldBe true
    }

    "verify startPolling" in {
      def ignoreDebugLogger[F[_]: Sync] =
        new TestLogger[F]() {
          override def debug(message: =>String): F[Unit] =
            Sync[F].unit
        }

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

      val result = TelegramClient[IO, DatabaseDriver.Cache](settings, ignoreDebugLogger[IO])
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
