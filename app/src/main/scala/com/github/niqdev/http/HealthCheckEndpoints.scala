package com.github.niqdev
package http

import cats.effect.Sync
import cats.implicits.toSemigroupKOps
import com.github.niqdev.model.Settings
import com.github.niqdev.service.HealthCheckService
import org.http4s.dsl.Http4sDsl
import org.http4s.{ HttpRoutes, Method }

private[http] sealed abstract class HealthCheckEndpoints[F[_]: Sync] extends Http4sDsl[F] {

  def statusEndpoint: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case Method.GET -> Root / "status" =>
        Ok("OK")
    }

  def infoEndpoint(service: HealthCheckService): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case Method.GET -> Root / "info" =>
        Ok(service.buildInformation[F])
    }

  def envEndpoint(settings: Settings): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case Method.GET -> Root / "env" =>
        Ok(Settings.obfuscate(settings))
    }

  def endpoints(service: HealthCheckService, settings: Settings): HttpRoutes[F] =
    statusEndpoint <+>
      infoEndpoint(service) <+>
      envEndpoint(settings)
}

private[http] object HealthCheckEndpoints {

  def apply[F[_]: Sync]: HealthCheckEndpoints[F] =
    new HealthCheckEndpoints[F] {}
}
