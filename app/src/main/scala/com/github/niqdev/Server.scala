package com.github.niqdev

import cats.effect.{ExitCode, IO, IOApp, Sync}
import cats.implicits.{catsSyntaxApply, catsSyntaxTuple2Semigroupal, toFlatMapOps, toFunctorOps}
import com.github.niqdev.algebra.MobileCarrierClient
import com.github.niqdev.http.HealthCheckEndpoint
import com.github.niqdev.model.MobileNetworkOperator.{ThreeIe, TimIt}
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli.http4sKleisliResponseSyntax

/*
 * TODO
 * parMapN Sync instead of IO https://typelevel.org/cats-effect/datatypes/io.html#parmapn
 * ValidateNel: accumulate errors
 *
 * tests ???
 */
object Server extends IOApp {

  private[this] def retrieveBalances[F[_]: Sync]: F[String] =
    (
      MobileCarrierClient[F, ThreeIe].balance("", ""),
      MobileCarrierClient[F, TimIt].balance("", "")
    ).mapN((threeBalance, timBalance) => s"Balances: [Three=$threeBalance][Tim=$timBalance]")

  private[this] def program[F[_]: Sync](log: Logger[F]): F[Unit] =
    for {
      _ <- log.info("Hello World")
      settings <- Settings.load[F]
      balances <- retrieveBalances[F]
      _ <- log.info(s"$settings")
      _ <- log.info(balances)
    } yield ()

  private[this] def server: IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(HealthCheckEndpoint[IO].routes.orNotFound)
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)

  private[this] def error[F[_]: Sync](log: Logger[F])(e: Throwable): F[ExitCode] =
    log.error(e)("Application failed") *> Sync[F].pure(ExitCode.Error)

  private[this] def success[F[_]: Sync, T](log: Logger[F]): T => F[ExitCode] =
    _ => log.info("Application succeeded") *> Sync[F].pure(ExitCode.Success)

  /**
    *
    */
  override def run(args: List[String]): IO[ExitCode] =
    Slf4jLogger
      .create[IO]
      .flatMap(log => program[IO](log) *> server.redeemWith(error[IO](log), success[IO, ExitCode](log)))

}
