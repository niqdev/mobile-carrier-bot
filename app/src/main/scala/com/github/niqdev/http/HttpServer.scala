package com.github.niqdev
package http

import cats.effect.{ ConcurrentEffect, ExitCode, Timer }
import com.github.niqdev.model.Settings
import com.github.niqdev.service.HealthCheckService
import fs2.Stream
import org.http4s.HttpRoutes
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import org.http4s.syntax.kleisli.http4sKleisliResponseSyntax

sealed abstract class HttpServer[F[_]: ConcurrentEffect: Timer] {

  private[http] def endpoints(settings: Settings): HttpRoutes[F] =
    HealthCheckEndpoints[F].endpoints(HealthCheckService(), settings)

  def start(settings: Settings): Stream[F, ExitCode] =
    BlazeServerBuilder[F]
      .bindHttp(settings.server.port.value, settings.server.host.value)
      // TODO middleware ???
      .withHttpApp(Logger.httpApp(logHeaders = true, logBody = true)(endpoints(settings).orNotFound))
      .serve

}

object HttpServer {
  def apply[F[_]: ConcurrentEffect: Timer]: HttpServer[F] =
    new HttpServer[F] {}
}
