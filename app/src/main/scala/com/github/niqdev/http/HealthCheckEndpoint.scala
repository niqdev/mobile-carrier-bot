package com.github.niqdev
package http

import cats.effect.Sync
import io.circe.generic.auto.exportEncoder
import io.circe.syntax.EncoderOps
import org.http4s.circe.jsonEncoder
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Method}

sealed abstract class HealthCheckEndpoint[F[_]: Sync] extends Http4sDsl[F] {

  private[http] def routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case Method.GET -> Root => Ok(buildInformation.asJson)
    }

  private[http] def buildInformation =
    BuildInformation(
      BuildInfo.name,
      BuildInfo.version,
      BuildInfo.scalaVersion,
      BuildInfo.builtAtString
    )
}

object HealthCheckEndpoint {
  def apply[F[_]: Sync]: HealthCheckEndpoint[F] = new HealthCheckEndpoint[F] {}
}
