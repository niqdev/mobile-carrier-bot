package com.github.niqdev

import cats.effect.Sync
import ciris.cats.catsMonadErrorToCiris
import ciris.refined.refTypeConfigDecoder
import ciris.{envF, loadConfig}
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.types.string.NonEmptyString

final case class Settings(environment: NonEmptyString,
                          httpPort: PosInt,
                          httpHost: NonEmptyString,
                          telegramApiToken: NonEmptyString)

object Settings {

  def load[F[_]: Sync]: F[Settings] =
    loadConfig(
      envF[F, NonEmptyString]("ENVIRONMENT"),
      envF[F, PosInt]("HTTP_PORT"),
      envF[F, NonEmptyString]("HTTP_HOST"),
      envF[F, NonEmptyString]("TELEGRAM_API_TOKEN")
    )(Settings.apply).orRaiseThrowable

}
