package com.github.niqdev
package model

import enumeratum.EnumEntry.{ Snakecase, Uppercase }
import enumeratum.{ Enum, EnumEntry }
import io.circe.Encoder

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
