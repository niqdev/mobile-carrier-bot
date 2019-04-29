package com.github.niqdev
package model

import enumeratum.EnumEntry.{ Snakecase, Uppercase }
import enumeratum.{ Enum, EnumEntry }
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

/**
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
