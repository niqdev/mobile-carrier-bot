package com.github.niqdev
package model
package telegram

import com.github.ghik.silencer.silent

// TODO user: telegram/slack mapping to internal user

@silent
sealed abstract class Command(value: String)
case object Start extends Command("/start")
case object Help  extends Command("/help")
// TODO check if user has operator credentials
case class Balance(operator: MobileNetworkOperator) extends Command("/balance")

trait TelegramParser {}
