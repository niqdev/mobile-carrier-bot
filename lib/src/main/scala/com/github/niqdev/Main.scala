package com.github.niqdev

import cats.effect.{ExitCode, IO, IOApp, Sync}
import cats.implicits.{toFlatMapOps, toFunctorOps}
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

object Main extends IOApp {

  private[this] def program[F[_] : Sync]: F[Unit] =
    for {
      log <- Slf4jLogger.create[F]
      _ <- log.info("Hello World")
    } yield ()

  override def run(args: List[String]): IO[ExitCode] =
    program[IO].as(ExitCode.Success)

}
