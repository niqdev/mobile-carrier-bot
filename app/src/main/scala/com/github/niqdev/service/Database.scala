package com.github.niqdev.service

import cats.effect.{ IO, Resource }
import doobie.h2.H2Transactor
import doobie.hikari.HikariTransactor
import doobie.{ ExecutionContexts, Transactor }

import scala.concurrent.ExecutionContext

object Database {

  // We need a ContextShift[IO] before we can construct a Transactor[IO]. The passed ExecutionContext
  // is where nonblocking operations will be executed.
  implicit val cs = IO.contextShift(ExecutionContext.global)

  // Resource yielding a transactor configured with a bounded connect EC and an unbounded
  // transaction EC. Everything will be closed and shut down cleanly after use.
  val transactor: Resource[IO, HikariTransactor[IO]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](32) // our connect EC
      te <- ExecutionContexts.cachedThreadPool[IO] // our transaction EC
      xa <- HikariTransactor.newHikariTransactor[IO](
        "org.h2.Driver", // driver classname
        "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", // connect URL
        "sa", // username
        "", // password
        ce, // await connection here
        te // execute JDBC operations here
      )
    } yield xa

  // Resource yielding a transactor configured with a bounded connect EC and an unbounded
  // transaction EC. Everything will be closed and shut down cleanly after use.
  val transactor0: Resource[IO, H2Transactor[IO]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](32) // our connect EC
      te <- ExecutionContexts.cachedThreadPool[IO] // our transaction EC
      xa <- H2Transactor.newH2Transactor[IO](
        "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", // connect URL
        "sa", // username
        "", // password
        ce, // await connection here
        te // execute JDBC operations here
      )
    } yield xa

  val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", // driver classname
    "jdbc:postgresql:world", // connect URL (driver-specific)
    "postgres", // user
    "" // password
  )

}
