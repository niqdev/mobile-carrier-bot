package com.github.niqdev

import cats.effect.Sync
import ciris.cats.catsMonadErrorToCiris
import ciris.refined.refTypeConfigDecoder
import ciris.{envF, loadConfig}
import eu.timepit.refined.types.string.NonEmptyString

case class Settings(environment: NonEmptyString)

object Settings {

  def load[F[_]: Sync]: F[Settings] =
    loadConfig(
      envF[F, NonEmptyString]("ENVIRONMENT")
    )(Settings.apply).orRaiseThrowable

}
