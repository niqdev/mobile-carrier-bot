package com.github.niqdev
package http

import cats.effect.Sync
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Method}

sealed abstract class HealthCheckEndpoint[F[_]: Sync] extends Http4sDsl[F] {

  def routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case Method.GET -> Root => Ok()
    }

}

object HealthCheckEndpoint {
  def apply[F[_]: Sync]: HealthCheckEndpoint[F] = new HealthCheckEndpoint[F] {}
}
