package com.github.niqdev
package model

import enumeratum.EnumEntry.Uppercase
import enumeratum.{Enum, EnumEntry}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto.autoRefineV
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.string.Url

/**
  * https://en.wikipedia.org/wiki/List_of_mobile_network_operators
  * https://en.wikipedia.org/wiki/List_of_country_calling_codes
  */
sealed abstract class MobileNetworkOperator(countryCallingCode: Int Refined Positive, website: String Refined Url) extends EnumEntry

object MobileNetworkOperator extends Enum[MobileNetworkOperator] {

  // macro
  val values = findValues

  case object ThreeIe extends MobileNetworkOperator(countryCallingCode = 353, website = "https://www.three.ie") with Uppercase
  case object TimIt extends MobileNetworkOperator(countryCallingCode = 39, website = "https://www.tim.it") with Uppercase

  final type ThreeIe = ThreeIe.type
  final type TimIt = TimIt.type
}
