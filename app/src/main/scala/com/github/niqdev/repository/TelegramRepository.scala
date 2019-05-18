package com.github.niqdev
package repository

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.syntax.flatMap.toFlatMapOps
import com.github.niqdev.model.DatabaseDriver
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

trait TelegramRepository[F[_], D] {
  def setOffset(offset: Long): F[Unit]
  def getOffset: F[Long]
}

object TelegramRepository extends TelegramRepositoryInstances {

  def apply[F[_], D](
    implicit D: TelegramRepository[F, D]
  ): TelegramRepository[F, D] = D
}

sealed trait TelegramRepositoryInstances {

  // impure
  private implicit def logger[F[_]: Sync]: Logger[F] =
    Slf4jLogger.getLogger[F]

  implicit def cacheTelegramRepository[F[_]: Sync]: TelegramRepository[F, DatabaseDriver.Cache] =
    new TelegramRepository[F, DatabaseDriver.Cache] {

      // preserve referential transparency since constructor is private
      private val ref: Ref[F, Long] =
        Ref.unsafe[F, Long](0)

      override def setOffset(offset: Long): F[Unit] =
        Sync[F]
          .defer(Logger[F].debug(s"[DatabaseDriver.Cache]: setOffset: $offset"))
          .flatMap(_ => ref.set(offset))

      override def getOffset: F[Long] =
        Sync[F]
          .defer(Logger[F].debug(s"[DatabaseDriver.Cache]: getOffset: ${ref.get}"))
          .flatMap(_ => ref.get)
    }

}
