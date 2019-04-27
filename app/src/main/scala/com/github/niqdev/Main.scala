package com.github.niqdev

import java.util.concurrent.{ ExecutorService, Executors, TimeUnit }

import cats.effect._
import cats.implicits.toFunctorOps
import cats.syntax.show.toShow
import com.github.ghik.silencer.silent
import com.github.niqdev.http.Http
import com.github.niqdev.model.Settings
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.client.Client
import org.http4s.server.Server
//import cats.implicits.catsSyntaxApply
import cats.effect.{ IO, Timer }
import fs2.Stream

import scala.concurrent.ExecutionContext

object Main extends IOApp.WithContext {

  Stream.

  import scala.concurrent.duration.DurationInt

  def myStream[F[_]: Sync: Timer] =
    Stream
      .eval(Sync[F].delay {
        println("streammmmm")
      })
      .flatMap(_ => Stream.sleep(2.seconds))
      .repeat
      .compile.drain

  @silent
  def start[F[_]: ConcurrentEffect: Timer]: Resource[F, (Client[F], Server[F])] =
    for {
      log <- Resource.liftF(Slf4jLogger.create[F])
      settings <- Resource.liftF(Settings.load[F])
      _ <- Resource.liftF(log.debug(s"Settings: ${settings.show}"))
      client <- Http[F].client(executionContext)
      server <- Http[F].server(settings)
      _ <- Resource.liftF(myStream)
    } yield (client, server)

  override protected def executionContextResource: Resource[SyncIO, ExecutionContext] = {
    val acquire = SyncIO(Executors.newCachedThreadPool())
    val release: ExecutorService => SyncIO[Unit] = pool =>
      SyncIO {
        pool.shutdown()
        pool.awaitTermination(10, TimeUnit.SECONDS)
        ()
      }

    Resource
      .make(acquire)(release)
      .map(ExecutionContext.fromExecutorService)
  }

  override def run(args: List[String]): IO[ExitCode] =
    //start[IO].use(_ => IO.never).as(ExitCode.Success)
    (for {
      log <- Resource.liftF(Slf4jLogger.create[IO])
      settings <- Resource.liftF(Settings.load[IO])
      _ <- Resource.liftF(log.debug(s"Settings: ${settings.show}"))
      //client <- Http[IO].client(executionContext)
      //_ <- Resource.liftF(TelegramService.apply.poll(client, settings, executionContext))
      _ <- Resource.liftF(myStream[IO])
    } yield ()).use(_ => IO.never).as(ExitCode.Success)
}
