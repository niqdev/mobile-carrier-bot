package com.github.niqdev
package model

import cats.effect.{ Resource, Sync }
import cats.{ Applicative, Show }
import ciris.cats.catsMonadErrorToCiris
import ciris.refined.refTypeConfigDecoder
import ciris.{ envF, loadConfig }
import eu.timepit.refined.types.net.PortNumber
import eu.timepit.refined.types.numeric.{ PosInt, PosLong }
import eu.timepit.refined.types.string.NonEmptyString
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

final case class Settings(
  logLevel: LogLevel,
  environment: Environment,
  server: ServerSettings,
  telegram: TelegramSettings,
  database: DatabaseSettings
)

final case class ServerSettings(
  port: PortNumber,
  host: NonEmptyString
)

final case class TelegramSettings(
  apiToken: NonEmptyString,
  polling: PosLong,
  baseUri: String = "https://api.telegram.org"
) {
  def uri = s"$baseUri/bot${apiToken.value}"
}

// TODO refined
final case class DatabaseSettings(
  driverClassName: String,
  url: String,
  username: String,
  password: String,
  connectionPoolSize: PosInt
)

sealed trait SettingsInstances {

  import io.circe.generic.auto.exportEncoder
  import io.circe.refined.refinedEncoder

  implicit def settingsEntityEncoder[F[_]: Applicative]: EntityEncoder[F, Settings] =
    jsonEncoderOf[F, Settings]

  implicit val settingsShow: Show[Settings] =
    (settings: Settings) => s"""
        |# global
        |LOG_LEVEL=${settings.logLevel.entryName}
        |ENVIRONMENT=${settings.environment.entryName}
        |# server
        |HTTP_PORT=${settings.server.port}
        |HTTP_HOST=${settings.server.host}
        |# telegram
        |TELEGRAM_API_TOKEN=${settings.telegram.apiToken}
        |TELEGRAM_POLLING_SECONDS=${settings.telegram.polling}
        |# db
        |DB_DRIVER_CLASS_NAME=${settings.database.driverClassName}
        |DB_URL=${settings.database.url}
        |DB_USERNAME=${settings.database.username}
        |DB_PASSWORD=${settings.database.password}
        |DB_CONNECTION_POOL_SIZE=${settings.database.connectionPoolSize}
      """.stripMargin
}

object Settings extends SettingsInstances {

  import eu.timepit.refined.auto.autoRefineV
  import ciris.enumeratum.enumEntryConfigDecoder

  // TODO move in case class or default parameter
  private[this] val defaultLogLevel: LogLevel               = LogLevel.Debug
  private[this] val defaultEnvironment: Environment         = Environment.Local
  private[this] val defaultPort: PortNumber                 = 8080
  private[this] val defaultHost: NonEmptyString             = "localhost"
  private[this] val defaultTelegramApiToken: NonEmptyString = "API_TOKEN"
  private[this] val defaultTelegramPolling: PosLong         = 5L
  private[this] val defaultDbDriverClassName: String        = "org.h2.Driver"
  private[this] val defaultDbUrl: String                    = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
  private[this] val defaultDbUsername: String               = "h2Username"
  private[this] val defaultDbPassword: String               = "h2Password"
  private[this] val defaultDbConnectionPoolSize: PosInt     = 32

  def apply(
    logLevel: LogLevel,
    environment: Environment,
    httpPort: PortNumber,
    httpHost: NonEmptyString,
    telegramApiToken: NonEmptyString,
    telegramPolling: PosLong,
    driverClassName: String,
    url: String,
    username: String,
    password: String,
    connectionPoolSize: PosInt
  ): Settings =
    Settings(
      logLevel,
      environment,
      ServerSettings(httpPort, httpHost),
      TelegramSettings(telegramApiToken, telegramPolling),
      DatabaseSettings(driverClassName, url, username, password, connectionPoolSize)
    )

  def loadF[F[_]: Sync]: F[Settings] =
    loadConfig(
      envF[F, LogLevel]("LOG_LEVEL")
        .orValue(defaultLogLevel),
      envF[F, Environment]("ENVIRONMENT")
        .orValue(defaultEnvironment),
      envF[F, PortNumber]("HTTP_PORT")
        .orValue(defaultPort),
      envF[F, NonEmptyString]("HTTP_HOST")
        .orValue(defaultHost),
      envF[F, NonEmptyString]("TELEGRAM_API_TOKEN")
        .orValue(defaultTelegramApiToken),
      envF[F, PosLong]("TELEGRAM_POLLING_SECONDS")
        .orValue(defaultTelegramPolling),
      envF[F, String]("DB_DRIVER_CLASS_NAME")
        .orValue(defaultDbDriverClassName),
      envF[F, String]("DB_URL")
        .orValue(defaultDbUrl),
      envF[F, String]("DB_USERNAME")
        .orValue(defaultDbUsername),
      envF[F, String]("DB_PASSWORD")
        .orValue(defaultDbPassword),
      envF[F, PosInt]("DB_CONNECTION_POOL_SIZE")
        .orValue(defaultDbConnectionPoolSize)
    )(Settings.apply).orRaiseThrowable

  def load[F[_]: Sync]: Resource[F, Settings] =
    Resource.liftF(loadF)

}