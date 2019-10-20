package com.github.niqdev
package http

import cats.effect.Sync
import cats.implicits.toSemigroupKOps
import com.github.niqdev.model.{ Environment, Settings }
import com.github.niqdev.service.HealthCheckService
import org.http4s.dsl.Http4sDsl
import org.http4s.{ HttpRoutes, Method }

private[http] sealed abstract class HealthCheckEndpoints[F[_]: Sync] extends Http4sDsl[F] {

  def statusEndpoint: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case Method.GET -> Root / "status" =>
        Ok(Ok.reason)
    }

  def infoEndpoint(service: HealthCheckService): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case Method.GET -> Root / "info" =>
        Ok(service.buildInformation[F])
    }

  def envEndpoint(settings: Settings): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case Method.GET -> Root / "env" =>
        settings.environment match {
          case Environment.Local =>
            Ok(settings)
          case _ =>
            NotFound(NotFound.reason)
        }
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
