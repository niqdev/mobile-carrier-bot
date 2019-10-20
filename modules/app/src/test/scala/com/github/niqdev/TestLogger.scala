package com.github.niqdev

import cats.effect.Sync
import com.github.ghik.silencer.silent
import io.chrisdavenport.log4cats.Logger

@silent
abstract class TestLogger[F[_]](implicit F: Sync[F]) extends Logger[F] {

  override def error(message: =>String): F[Unit] =
    F.raiseError(???)

  override def warn(message: =>String): F[Unit] =
    F.raiseError(???)

  override def info(message: =>String): F[Unit] =
    F.raiseError(???)

  override def debug(message: =>String): F[Unit] =
    F.raiseError(???)

  override def trace(message: =>String): F[Unit] =
    F.raiseError(???)

  override def error(t: Throwable)(message: =>String): F[Unit] =
    F.raiseError(???)

  override def warn(t: Throwable)(message: =>String): F[Unit] =
    F.raiseError(???)

  override def info(t: Throwable)(message: =>String): F[Unit] =
    F.raiseError(???)

  override def debug(t: Throwable)(message: =>String): F[Unit] =
    F.raiseError(???)

  override def trace(t: Throwable)(message: =>String): F[Unit] =
    F.raiseError(???)
}
