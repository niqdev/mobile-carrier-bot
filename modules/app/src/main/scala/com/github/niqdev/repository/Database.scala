package com.github.niqdev
package repository

import cats.effect.{ Async, ContextShift, Resource }
import com.github.niqdev.model.DatabaseSettings
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

// TODO Flyway
// https://flywaydb.org
// https://github.com/pauljamescleary/scala-pet-store/blob/master/src/main/scala/io/github/pauljamescleary/petstore/config/DatabaseConfig.scala#L24
// https://github.com/jaspervz/todo-http4s-doobie/blob/master/src/main/scala/db/Database.scala#L13
object Database {

  /*
   * https://tpolecat.github.io/doobie/docs/14-Managing-Connections.html#using-a-hikaricp-connection-pool
   */
  def transactor[F[_]: Async: ContextShift](settings: DatabaseSettings): Resource[F, HikariTransactor[F]] =
    for {
      connectEC  <- ExecutionContexts.fixedThreadPool[F](settings.connectionPoolSize.value)
      transactEC <- ExecutionContexts.cachedThreadPool[F]
      xa <- HikariTransactor.newHikariTransactor[F](
        settings.driver.className.value,
        settings.url,
        settings.username,
        settings.password,
        connectEC,
        transactEC
      )
    } yield xa

}

/*

lastpass-operator
 * telegram_api_key
 * encryption_keys

phones
id|mobile_carrier|phone_number|username|password

telegram_users
telegram_id|phone_id

slack_users
slack_id|phone_id

 */
