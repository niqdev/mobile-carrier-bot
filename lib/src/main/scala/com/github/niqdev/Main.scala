package com.github.niqdev

import cats.effect.{ExitCode, IO, IOApp, Sync}
import cats.implicits.{catsSyntaxApply, catsSyntaxTuple2Semigroupal, toFlatMapOps, toFunctorOps}
import com.github.niqdev.algebra.MobileCarrierClient
import com.github.niqdev.model.{Settings, Three, Tim}
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder

/*
 * TODO
 * parMapN Sync instead of IO https://typelevel.org/cats-effect/datatypes/io.html#parmapn
 * ValidateNel: accumulate errors
 *
 * tests ???
 */
object Main extends IOApp {

  private[this] def retrieveBalances[F[_] : Sync]: F[String] = (
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

  val helloWorldService = HttpRoutes.of[IO] {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name.")
  }.orNotFound

  private[this] def server: IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(Main.helloWorldService)
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)

  private[this] def error[F[_] : Sync](log: Logger[F])(e: Throwable): F[ExitCode] =
    log.error(e)("Application failed") *> Sync[F].pure(ExitCode.Error)

  private[this] def success[F[_] : Sync, T](log: Logger[F]): T => F[ExitCode] =
    _ => log.info("Application succeeded") *> Sync[F].pure(ExitCode.Success)

  /**
    *
    */
  override def run(args: List[String]): IO[ExitCode] =
    Slf4jLogger.create[IO].flatMap(log =>
      program[IO](log) *> server.redeemWith(error[IO](log), success[IO, ExitCode](log)))

}
