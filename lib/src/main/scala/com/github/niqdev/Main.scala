package com.github.niqdev

import cats.effect.{ExitCode, IO, IOApp, Sync}
import cats.implicits.{catsSyntaxApply, toFlatMapOps, toFunctorOps}
import com.github.niqdev.algebra.MobileCarrierClient
import com.github.niqdev.model.{Settings, Three, Tim}
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

object Main extends IOApp {

  // TODO run in parallel: parSequence
  private[this] def program[F[_] : Sync](log: Logger[F]): F[Unit] =
    for {
      _ <- log.info("Hello World")
      settings <- Settings.load[F]
      threeBalance <- MobileCarrierClient[F, Three].balance("", "")
      timBalance <- MobileCarrierClient[F, Tim].balance("", "")
      _ <- log.info(s"$settings")
      _ <- log.info(s"Balances: [Three=$threeBalance][Tim=$timBalance]")
    } yield ()

  private[this] def error[F[_] : Sync](log: Logger[F])(e: Throwable): F[ExitCode] =
    log.error(e)("Application failed") *> Sync[F].pure(ExitCode.Error)

  private[this] def success[F[_] : Sync](log: Logger[F]): Unit => F[ExitCode] =
    _ => log.info("Application succeeded") *> Sync[F].pure(ExitCode.Success)

  /**
    *
    */
  override def run(args: List[String]): IO[ExitCode] =
    Slf4jLogger.create[IO].flatMap(log =>
      program[IO](log).redeemWith(error[IO](log), success[IO](log)))

}
