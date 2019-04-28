package com.github.niqdev

import java.util.concurrent.{ ExecutorService, Executors, TimeUnit }

import cats.effect.{ ExitCode, IO, IOApp, Sync, Timer, _ }
import cats.implicits.{ catsSyntaxApply, toFlatMapOps, toFunctorOps }
import cats.syntax.show.toShow
import com.github.niqdev.http.{ HttpServer, TelegramClient }
import com.github.niqdev.model.Settings
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

import scala.concurrent.ExecutionContext

object Main extends IOApp.WithContext {

  def start[F[_]: ConcurrentEffect: Timer](log: Logger[F]): F[Unit] =
    for {
      settings <- Settings.load[F]
      _        <- log.debug(s"Settings: ${settings.show}")
      _ <- HttpServer[F]
        .start(settings)
        .merge(TelegramClient[F].startPolling(settings.telegram, executionContext))
        .compile
        .drain
    } yield ()

  private[this] def error[F[_]: Sync](log: Logger[F])(e: Throwable): F[ExitCode] =
    log.error(e)("Application failed") *> Sync[F].pure(ExitCode.Error)

  private[this] def success[F[_]: Sync]: Unit => F[ExitCode] =
    _ => Sync[F].pure(ExitCode.Success)

  /**
    *
    */
  override def run(args: List[String]): IO[ExitCode] =
    Slf4jLogger
      .create[IO]
      .flatMap(log => start[IO](log).redeemWith(error[IO](log), success[IO]))

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

}
