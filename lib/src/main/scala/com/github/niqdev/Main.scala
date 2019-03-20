package com.github.niqdev

import cats.effect.{ExitCode, IO, IOApp, Sync}
import cats.implicits.{catsSyntaxApply, toFlatMapOps, toFunctorOps}
import ciris.cats.catsMonadErrorToCiris
import ciris.refined.refTypeConfigDecoder
import ciris.{envF, loadConfig}
import com.github.niqdev.model.Settings
import eu.timepit.refined.types.string.NonEmptyString
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

object Main extends IOApp {

  private[this] def program[F[_] : Sync](log: Logger[F]): F[Unit] =
    for {
      _ <- log.info("Hello World")
      settings <- loadConfig(
        envF[F, NonEmptyString]("ENVIRONMENT")
      )(Settings.apply).orRaiseThrowable
      _ <- log.info(s"$settings")
    } yield ()

  private[this] def success[F[_] : Sync](log: Logger[F]): Unit => F[ExitCode] =
    _ => log.info("Application succeeded") *> Sync[F].delay(ExitCode.Success)

  private[this] def error[F[_] : Sync](log: Logger[F])(e: Throwable): F[ExitCode] =
    log.error(e)("Application failed") *> Sync[F].delay(ExitCode.Error)

  override def run(args: List[String]): IO[ExitCode] =
    Slf4jLogger.create[IO].flatMap(log =>
      program[IO](log).redeemWith(error[IO](log), success[IO](log)))

}
