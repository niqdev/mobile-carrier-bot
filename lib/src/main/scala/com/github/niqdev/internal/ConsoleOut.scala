package com.github.niqdev
package internal

import cats.effect.Sync

trait ConsoleOut[F[_]] {

  def println[A: Show](a: A): F[Unit]

}

object ConsoleOut {

  def apply[F[_]](implicit F: ConsoleOut[F]): ConsoleOut[F] = F

  implicit def consoleOut[F[_]](implicit F: Sync[F]): ConsoleOut[F] =
    new ConsoleOut[F] {
      override def println[A: Show](a: A): F[Unit] =
        F.delay(scala.Console.println(a))
    }

}

