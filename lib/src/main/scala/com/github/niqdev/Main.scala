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

  private[this] def program[F[_] : Sync]: F[Unit] =
    for {
      log <- Slf4jLogger.create[F]
      _ <- log.info("Hello World")
      settings <- loadConfig(
        envF[F, NonEmptyString]("ENVIRONMENT")
      )(Settings.apply).orRaiseThrowable
      _ <- log.info(s"Settings: $settings")
    } yield ()

  private[this] def success[F[_] : Sync](implicit L: Logger[F]): Unit => F[ExitCode] =
    _ => Logger[F].info("Application succeeded") *> Sync[F].delay(ExitCode.Success)

  private[this] def error[F[_] : Sync : Logger](e: Throwable): F[ExitCode] =
    Logger[F].error(e)("Application failed") *> Sync[F].delay(ExitCode.Error)

  override def run(args: List[String]): IO[ExitCode] =
    program[IO].redeemWith(error[IO], success[IO])

}
