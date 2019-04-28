package com.github.niqdev
package http

import cats.data.Kleisli
import cats.effect.{ ConcurrentEffect, ExitCode, Timer }
import com.github.niqdev.model.Settings
import com.github.niqdev.service.HealthCheckService
import fs2.Stream
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli.http4sKleisliResponseSyntax
import org.http4s.{ Request, Response }

sealed abstract class HttpServer[F[_]](implicit E: ConcurrentEffect[F], T: Timer[F]) {

  private[http] def endpoints(settings: Settings): Kleisli[F, Request[F], Response[F]] =
    HealthCheckEndpoints.endpoints[F](HealthCheckService[F](), settings).orNotFound

  def start(settings: Settings): Stream[F, ExitCode] =
    BlazeServerBuilder[F]
      .bindHttp(settings.server.port.value, settings.server.host.value)
      .withHttpApp(endpoints(settings))
      .serve

}

object HttpServer {
  def apply[F[_]](implicit E: ConcurrentEffect[F], T: Timer[F]): HttpServer[F] = new HttpServer[F] {}
}
