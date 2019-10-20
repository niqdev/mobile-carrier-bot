package com.github.niqdev
package model

import cats.Applicative
import cats.effect.Sync
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import io.circe.{ Decoder, Encoder }
import org.http4s.circe.{ jsonEncoderOf, jsonOf }
import org.http4s.{ EntityDecoder, EntityEncoder }

final case class BuildInformation(name: String, version: String, scalaVersion: String, buildTime: String)

object BuildInformation {

  implicit val buildInformationEncoder: Encoder[BuildInformation] =
    deriveEncoder[BuildInformation]

  implicit def buildInformationEntityEncoder[F[_]: Applicative]: EntityEncoder[F, BuildInformation] =
    jsonEncoderOf[F, BuildInformation]

  implicit val buildInformationDecoder: Decoder[BuildInformation] =
    deriveDecoder[BuildInformation]

  implicit def buildInformationEntityDecoder[F[_]: Sync]: EntityDecoder[F, BuildInformation] =
    jsonOf[F, BuildInformation]

}
