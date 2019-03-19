package com.github.niqdev

import cats.effect.IO
import com.github.niqdev.internal.ConsoleOut

object Main extends App {

  val hello = ConsoleOut[IO].println("Hello World")

  val program: IO[Unit] =
    for {
      _ <- hello
      _ <- hello
    } yield ()

  program.unsafeRunSync()

}
