package com.github.niqdev
package model

// TODO enumeratum: country, prefix, ...
sealed trait MobileCarrier
case object Three extends MobileCarrier
case object Tim extends MobileCarrier
