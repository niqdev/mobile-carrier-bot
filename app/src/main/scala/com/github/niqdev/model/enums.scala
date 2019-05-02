package com.github.niqdev
package model

import enumeratum.EnumEntry.{ Lowercase, Snakecase, Uppercase }
import enumeratum.{ Enum, EnumEntry }
import eu.timepit.refined.auto.autoRefineV
import eu.timepit.refined.types.string.NonEmptyString
import io.circe.Encoder

/**
  *
  */
sealed trait Environment extends EnumEntry with Snakecase with Uppercase

sealed trait EnvironmentInstances {

  implicit val encodeEnvironment: Encoder[Environment] =
    Encoder.encodeString.contramap[Environment](_.entryName)
}

object Environment extends Enum[Environment] with EnvironmentInstances {

  // macro
  val values = findValues

  case object Local       extends Environment
  case object Development extends Environment
  case object Production  extends Environment

}

/*
 * [[org.slf4j.event.Level LogLevel]]
 */
sealed trait LogLevel extends EnumEntry with Snakecase with Uppercase

sealed trait LogLevelInstances {

  implicit val encodeLogLevel: Encoder[LogLevel] =
    Encoder.encodeString.contramap[LogLevel](_.entryName)
}

object LogLevel extends Enum[LogLevel] with LogLevelInstances {

  // macro
  val values = findValues

  case object Debug extends LogLevel
  case object Info  extends LogLevel
  case object Warn  extends LogLevel
  case object Error extends LogLevel

}

/**
  *
  */
sealed abstract class DatabaseDriver(val className: NonEmptyString) extends EnumEntry with Lowercase

sealed trait DatabaseDriverInstances {

  implicit val encodeDatabaseDriver: Encoder[DatabaseDriver] =
    Encoder.encodeString.contramap[DatabaseDriver](_.className.value)
}

object DatabaseDriver extends Enum[DatabaseDriver] with DatabaseDriverInstances {

  // macro
  val values = findValues

  case object PostgreSQL extends DatabaseDriver("org.postgresql.Driver")
  case object H2         extends DatabaseDriver("org.h2.Driver")
  case object Cache      extends DatabaseDriver("cats.effect.concurrent.Ref")

  final type PostgreSQL = PostgreSQL.type
  final type H2         = H2.type
  final type Cache      = Cache.type
}
