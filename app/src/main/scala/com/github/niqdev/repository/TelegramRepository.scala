package com.github.niqdev
package repository

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.syntax.flatMap.toFlatMapOps
import com.github.niqdev.model.DatabaseDriver

trait TelegramRepository[F[_], D] {
  def setOffset(offset: Long): F[Unit]
  def getOffset: F[Long]
}

object TelegramRepository extends TelegramRepositoryInstances {
  def apply[F[_], D](
    implicit D: TelegramRepository[F, D]
  ): TelegramRepository[F, D] = D
}

// TODO log
sealed trait TelegramRepositoryInstances {

  implicit def cacheTelegramRepository[F[_]: Sync]: TelegramRepository[F, DatabaseDriver.Cache] =
    new TelegramRepository[F, DatabaseDriver.Cache] {

      // preserve referential transparency since constructor is private
      private val ref: Ref[F, Long] =
        Ref.unsafe[F, Long](0)

      override def setOffset(offset: Long): F[Unit] =
        Sync[F]
          .delay(println(s"[DatabaseDriver.Cache]: setOffset: $offset"))
          .flatMap(_ => ref.set(offset))

      override def getOffset: F[Long] =
        Sync[F]
          .delay(println(s"[DatabaseDriver.Cache]: getOffset: ${ref.get}"))
          .flatMap(_ => ref.get)
    }

}