package com.github.niqdev

final case class BuildInformation(name: String, version: String, scalaVersion: String, buildTime: String)

/**
 * https://core.telegram.org/bots/api#update
 */
final case class TelegramUpdate()
