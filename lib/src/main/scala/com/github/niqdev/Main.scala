package com.github.niqdev

import cats.effect.IO

object Main extends App {

  val ioa = IO {
    println("hello")
  }

  val program: IO[Unit] =
    for {
      _ <- ioa
      _ <- ioa
    } yield ()

  program.unsafeRunSync()

}
