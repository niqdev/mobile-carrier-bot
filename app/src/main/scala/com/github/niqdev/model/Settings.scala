package com.github.niqdev.model

import cats.effect.Sync
import cats.{ Applicative, Show }
import ciris.cats.catsMonadErrorToCiris
import ciris.refined.refTypeConfigDecoder
import ciris.{ envF, loadConfig }
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.types.string.NonEmptyString
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

final case class Settings(environment: NonEmptyString,
                          httpPort: PosInt,
                          httpHost: NonEmptyString,
                          telegramApiToken: NonEmptyString,
                          // TODO FiniteDuration
                          telegramPolling: Long)

sealed trait SettingsInstances {

  import io.circe.refined.refinedEncoder

  implicit val settingsEncoder: Encoder[Settings] =
    deriveEncoder[Settings]

  implicit def settingsEntityEncoder[F[_]: Applicative]: EntityEncoder[F, Settings] =
    jsonEncoderOf[F, Settings]

  implicit val settingsShow: Show[Settings] =
    (settings: Settings) => s"""
         |ENVIRONMENT=${settings.environment}
         |HTTP_PORT=${settings.httpPort}
         |HTTP_HOST=${settings.httpHost}
         |TELEGRAM_API_TOKEN=${settings.telegramApiToken}
         |TELEGRAM_POLLING=${settings.telegramPolling}
       """.stripMargin

}

object Settings extends SettingsInstances {

  import eu.timepit.refined.auto.autoRefineV

  //import scala.concurrent.duration.DurationInt

  private[this] val defaultEnvironment: NonEmptyString      = "dev"
  private[this] val defaultPort: PosInt                     = 8080
  private[this] val defaultHost: NonEmptyString             = "localhost"
  private[this] val defaultTelegramApiToken: NonEmptyString = "API_TOKEN"
  private[this] val defaultTelegramPolling: Long            = 5

  def load[F[_]: Sync]: F[Settings] =
    loadConfig(
      envF[F, NonEmptyString]("ENVIRONMENT")
        .orValue(defaultEnvironment),
      envF[F, PosInt]("HTTP_PORT")
        .orValue(defaultPort),
      envF[F, NonEmptyString]("HTTP_HOST")
        .orValue(defaultHost),
      envF[F, NonEmptyString]("TELEGRAM_API_TOKEN")
        .orValue(defaultTelegramApiToken),
      envF[F, Long]("TELEGRAM_POLLING")
        .orValue(defaultTelegramPolling)
    )(Settings.apply).orRaiseThrowable

  def obfuscate(settings: Settings): Settings =
    if (settings.environment != defaultEnvironment)
      settings.copy(telegramApiToken = defaultTelegramApiToken)
    else
      settings

}
