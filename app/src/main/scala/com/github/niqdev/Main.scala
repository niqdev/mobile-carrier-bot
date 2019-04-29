package com.github.niqdev

import java.util.concurrent.{ ExecutorService, Executors, TimeUnit }

import cats.effect._
import cats.implicits.catsSyntaxApply
import cats.syntax.show.toShow
import com.github.ghik.silencer.silent
import com.github.niqdev.http.{ HttpResource, TelegramClient }
import com.github.niqdev.model.{ DatabaseDriver, Settings }
import com.github.niqdev.repository.{ Database, TelegramRepository }
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.server.Server

import scala.concurrent.ExecutionContext

object Main extends IOApp.WithContext {

  @silent
  def start[F[_]: ContextShift: ConcurrentEffect: Timer](log: Logger[F]): Resource[F, Server[F]] =
    for {
      settings <- Settings.load[F]
      xa       <- Database.transactor[F](settings.database)
      _                  = log.debug(s"Settings: ${settings.show}")
      telegramRepository = TelegramRepository[F, DatabaseDriver.Cache]
      client <- HttpResource[F].client(executionContext)
      server <- HttpResource[F].server(settings)
      _      <- TelegramClient.startPolling[F](client, settings.telegram)
    } yield server

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
      .flatMap(log => start[IO](log).use(_ => IO.never).redeemWith(error[IO](log), success[IO]))

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
