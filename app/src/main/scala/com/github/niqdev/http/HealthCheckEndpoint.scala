package com.github.niqdev
package http

import cats.effect.Sync
import com.github.niqdev.service.HealthCheckService
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Method}

sealed abstract class HealthCheckEndpoint[F[_]: Sync] extends Http4sDsl[F] {

  def routes(service: HealthCheckService[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case Method.GET -> Root =>
        Ok(service.buildInformation)
    }
}

object HealthCheckEndpoint {
  def apply[F[_]: Sync]: HealthCheckEndpoint[F] = new HealthCheckEndpoint[F] {}
}
