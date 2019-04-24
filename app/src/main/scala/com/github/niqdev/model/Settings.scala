package com.github.niqdev.model

import cats.Show
import cats.effect.Sync
import ciris.cats.catsMonadErrorToCiris
import ciris.refined.refTypeConfigDecoder
import ciris.{envF, loadConfig}
import eu.timepit.refined.auto.autoRefineV
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.types.string.NonEmptyString

final case class Settings(environment: NonEmptyString,
                          httpPort: PosInt,
                          httpHost: NonEmptyString,
                          telegramApiToken: NonEmptyString)

object Settings {

  private[this] val defaultEnvironment: NonEmptyString = "dev"
  private[this] val defaultPort: PosInt = 8080
  private[this] val defaultHost: NonEmptyString = "localhost"
  private[this] val defaultTelegramApiToken: NonEmptyString = "API_TOKEN"

  def load[F[_]: Sync]: F[Settings] =
    loadConfig(
      envF[F, NonEmptyString]("ENVIRONMENT")
        .orValue(defaultEnvironment),
      envF[F, PosInt]("HTTP_PORT")
        .orValue(defaultPort),
      envF[F, NonEmptyString]("HTTP_HOST")
        .orValue(defaultHost),
      envF[F, NonEmptyString]("TELEGRAM_API_TOKEN")
        .orValue(defaultTelegramApiToken)
    )(Settings.apply).orRaiseThrowable

  implicit val settingsShow: Show[Settings] =
    (settings: Settings) => s"""
         |ENVIRONMENT=${settings.environment}
         |HTTP_PORT=${settings.httpPort}
         |HTTP_HOST=${settings.httpHost}
         |TELEGRAM_API_TOKEN=${settings.telegramApiToken}
       """.stripMargin

}
