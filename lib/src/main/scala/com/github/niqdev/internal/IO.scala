package com.github.niqdev
package internal

class IO[A] private(a0: => A) {

  def run: A = a0

  // TODO monad laws
  def flatMap[B](f: A => IO[B]): IO[B] =
    //f(a)
    IO(f(run).run)

  // TODO monad laws
  def map[B](f: A => B): IO[B] =
    //IO(f(run))
    flatMap(a => IO(f(a)))

}

object IO {
  def apply[A](a: => A): IO[A] = new IO(a)
}
