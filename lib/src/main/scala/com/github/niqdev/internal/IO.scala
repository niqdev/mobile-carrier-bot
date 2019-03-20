package com.github.niqdev
package internal

class IO[A] private(a0: => A) {

  def run: A = a0

  // TODO verify monad laws
  def flatMap[B](f: A => IO[B]): IO[B] =
    f(run)
    //IO(f(run).run)

  // TODO verify monad laws
  def map[B](f: A => B): IO[B] =
    IO(f(run))
    //flatMap(a => IO(f(a)))

}

object IO {
  def apply[A](a: => A): IO[A] = new IO(a)
}
