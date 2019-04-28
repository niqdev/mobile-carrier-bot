package com.github.niqdev.model

import cats.effect.Sync
import cats.{ Applicative, Show }
import ciris.cats.catsMonadErrorToCiris
import ciris.refined.refTypeConfigDecoder
import ciris.{ envF, loadConfig }
import eu.timepit.refined.types.net.PortNumber
import eu.timepit.refined.types.numeric.PosLong
import eu.timepit.refined.types.string.NonEmptyString
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

final case class Settings(
  environment: Environment,
  server: ServerSettings,
  telegram: TelegramSettings
)

final case class ServerSettings(
  port: PortNumber,
  host: NonEmptyString
)

final case class TelegramSettings(
  apiToken: NonEmptyString,
  polling: PosLong
) {
  def uri = s"https://api.telegram.org/bot${apiToken.value}"
}

sealed trait SettingsInstances {

  import io.circe.generic.auto.exportEncoder
  import io.circe.refined.refinedEncoder

  implicit def settingsEntityEncoder[F[_]: Applicative]: EntityEncoder[F, Settings] =
    jsonEncoderOf[F, Settings]

  implicit val settingsShow: Show[Settings] =
    (settings: Settings) => s"""
         |# global settings
         |ENVIRONMENT=${settings.environment.entryName}
         |# server settings
         |HTTP_PORT=${settings.server.port}
         |HTTP_HOST=${settings.server.host}
         |# telegram settings
         |TELEGRAM_API_TOKEN=${settings.telegram.apiToken}
         |TELEGRAM_POLLING_SECONDS=${settings.telegram.polling}
       """.stripMargin
}

object Settings extends SettingsInstances {

  import eu.timepit.refined.auto.autoRefineV
  import ciris.enumeratum.enumEntryConfigDecoder

  private[this] val defaultEnvironment: Environment         = Environment.Local
  private[this] val defaultPort: PortNumber                 = 8080
  private[this] val defaultHost: NonEmptyString             = "localhost"
  private[this] val defaultTelegramApiToken: NonEmptyString = "API_TOKEN"
  private[this] val defaultTelegramPolling: PosLong         = 5L

  def apply(
    environment: Environment,
    httpPort: PortNumber,
    httpHost: NonEmptyString,
    telegramApiToken: NonEmptyString,
    telegramPolling: PosLong
  ): Settings =
    Settings(
      environment,
      ServerSettings(httpPort, httpHost),
      TelegramSettings(telegramApiToken, telegramPolling)
    )

  def load[F[_]: Sync]: F[Settings] =
    loadConfig(
      envF[F, Environment]("ENVIRONMENT")
        .orValue(defaultEnvironment),
      envF[F, PortNumber]("HTTP_PORT")
        .orValue(defaultPort),
      envF[F, NonEmptyString]("HTTP_HOST")
        .orValue(defaultHost),
      envF[F, NonEmptyString]("TELEGRAM_API_TOKEN")
        .orValue(defaultTelegramApiToken),
      envF[F, PosLong]("TELEGRAM_POLLING")
        .orValue(defaultTelegramPolling)
    )(Settings.apply).orRaiseThrowable
}
