package com.github.niqdev
package model

import cats.Applicative
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

final case class BuildInformation(name: String, version: String, scalaVersion: String, buildTime: String)

object BuildInformation {

  implicit val buildInformationEncoder: Encoder[BuildInformation] =
    deriveEncoder[BuildInformation]

  implicit def buildInformationEntityEncoder[F[_]: Applicative]: EntityEncoder[F, BuildInformation] =
    jsonEncoderOf[F, BuildInformation]
}
