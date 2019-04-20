package com.github.niqdev

import cats.effect.{ExitCode, IO, IOApp, Sync}
import cats.implicits.{catsSyntaxApply, toFlatMapOps, toFunctorOps}
import cats.syntax.all._
import com.github.niqdev.algebra.MobileCarrierClient
import com.github.niqdev.model.{Settings, Three, Tim}
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

object Main extends IOApp {

  def retrieveBalances[F[_] : Sync]: F[String] = (
    MobileCarrierClient[F, Three].balance("", ""),
    MobileCarrierClient[F, Tim].balance("", "")
  ).mapN((threeBalance, timBalance) => s"Balances: [Three=$threeBalance][Tim=$timBalance]")

  private[this] def program[F[_] : Sync](log: Logger[F]): F[Unit] =
    for {
      _ <- log.info("Hello World")
      settings <- Settings.load[F]
      balances <- retrieveBalances[F]
      _ <- log.info(s"$settings")
      _ <- log.info(balances)
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
