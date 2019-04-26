package com.github.niqdev
package http

import cats.effect.Sync
import cats.implicits.toSemigroupKOps
import com.github.niqdev.model.Settings
import com.github.niqdev.service.HealthCheckService
import org.http4s.dsl.Http4sDsl
import org.http4s.{ HttpRoutes, Method }

sealed abstract class HealthCheckEndpoints[F[_]: Sync] extends Http4sDsl[F] {

  private[http] def statusEndpoint: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case Method.GET -> Root / "status" =>
        Ok("OK")
    }

  private[http] def infoEndpoint(service: HealthCheckService[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case Method.GET -> Root / "info" =>
        Ok(service.buildInformation)
    }

  private[http] def configEndpoint(settings: Settings): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case Method.GET -> Root / "config" =>
        Ok(Settings.obfuscate(settings))
    }

  def endpoints(service: HealthCheckService[F], settings: Settings): HttpRoutes[F] =
    statusEndpoint <+>
      infoEndpoint(service) <+>
      configEndpoint(settings)
}

object HealthCheckEndpoints {

  def apply[F[_]: Sync]: HealthCheckEndpoints[F] =
    new HealthCheckEndpoints[F] {}

  def endpoints[F[_]: Sync](service: HealthCheckService[F], settings: Settings): HttpRoutes[F] =
    HealthCheckEndpoints[F].endpoints(service, settings)
}
