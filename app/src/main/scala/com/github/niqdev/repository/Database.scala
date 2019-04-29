package com.github.niqdev
package repository

import cats.effect.{ Async, ContextShift, Resource }
import com.github.niqdev.model.DatabaseSettings
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

// TODO Flyway
// https://flywaydb.org
// https://github.com/pauljamescleary/scala-pet-store/blob/master/src/main/scala/io/github/pauljamescleary/petstore/config/DatabaseConfig.scala#L24
object Database {

  /*
   * https://tpolecat.github.io/doobie/docs/14-Managing-Connections.html#using-a-hikaricp-connection-pool
   */
  def transactor[F[_]: Async: ContextShift](settings: DatabaseSettings): Resource[F, HikariTransactor[F]] =
    for {
      connectEC  <- ExecutionContexts.fixedThreadPool[F](settings.connectionPoolSize.value)
      transactEC <- ExecutionContexts.cachedThreadPool[F]
      xa <- HikariTransactor.newHikariTransactor[F](
        settings.driverClassName,
        settings.url,
        settings.username,
        settings.password,
        connectEC,
        transactEC
      )
    } yield xa

}
