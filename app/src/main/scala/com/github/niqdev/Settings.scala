package com.github.niqdev

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
  private[this] val invalidTelegramApiToken: NonEmptyString = "xxx"

  def load[F[_]: Sync]: F[Settings] =
    loadConfig(
      envF[F, NonEmptyString]("ENVIRONMENT")
        .orValue(defaultEnvironment),
      envF[F, PosInt]("HTTP_PORT")
        .orValue(defaultPort),
      envF[F, NonEmptyString]("HTTP_HOST")
        .orValue(defaultHost),
      envF[F, NonEmptyString]("TELEGRAM_API_TOKEN")
        .orValue(invalidTelegramApiToken)
    )(Settings.apply).orRaiseThrowable

}
