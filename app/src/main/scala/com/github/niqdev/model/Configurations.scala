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

final case class Configurations(environment: NonEmptyString,
                                httpPort: PosInt,
                                httpHost: NonEmptyString,
                                telegramApiToken: NonEmptyString)

sealed trait ConfigurationsInstances {

  import io.circe.refined.refinedEncoder

  implicit val configurationsEncoder: Encoder[Configurations] =
    deriveEncoder[Configurations]

  implicit def configurationsEntityEncoder[F[_]: Applicative]: EntityEncoder[F, Configurations] =
    jsonEncoderOf[F, Configurations]

  implicit val settingsShow: Show[Configurations] =
    (settings: Configurations) => s"""
         |ENVIRONMENT=${settings.environment}
         |HTTP_PORT=${settings.httpPort}
         |HTTP_HOST=${settings.httpHost}
         |TELEGRAM_API_TOKEN=${settings.telegramApiToken}
       """.stripMargin

}

object Configurations extends ConfigurationsInstances {

  import eu.timepit.refined.auto.autoRefineV

  private[this] val defaultEnvironment: NonEmptyString = "dev"
  private[this] val defaultPort: PosInt = 8080
  private[this] val defaultHost: NonEmptyString = "localhost"
  private[this] val defaultTelegramApiToken: NonEmptyString = "API_TOKEN"

  def load[F[_]: Sync]: F[Configurations] =
    loadConfig(
      envF[F, NonEmptyString]("ENVIRONMENT")
        .orValue(defaultEnvironment),
      envF[F, PosInt]("HTTP_PORT")
        .orValue(defaultPort),
      envF[F, NonEmptyString]("HTTP_HOST")
        .orValue(defaultHost),
      envF[F, NonEmptyString]("TELEGRAM_API_TOKEN")
        .orValue(defaultTelegramApiToken)
    )(Configurations.apply).orRaiseThrowable

}
