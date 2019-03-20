package com.github.niqdev

import com.github.niqdev.internal.{ConsoleOut, IO}

object Main extends App {

  //val hello = ConsoleOut[IO].println("Hello World")
  val hello1 = IO { println("Hello World1") }
  val hello2 = IO { println("Hello World2") }

  val program: IO[Unit] =
    for {
      _ <- hello1
      _ <- hello1
      _ <- hello2
      _ <- hello2
    } yield ()

  program.run

}
