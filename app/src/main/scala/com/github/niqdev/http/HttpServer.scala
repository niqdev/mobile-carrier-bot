package com.github.niqdev
package http

import cats.data.Kleisli
import cats.effect.{ConcurrentEffect, Timer}
import com.github.niqdev.model.Settings
import com.github.niqdev.service.HealthCheckService
import fs2.Stream
import org.http4s.server.Server
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli.http4sKleisliResponseSyntax
import org.http4s.{Request, Response}

sealed abstract class HttpServer[F[_]](implicit E: ConcurrentEffect[F], T: Timer[F]) {

  private[http] def endpoints(settings: Settings): Kleisli[F, Request[F], Response[F]] =
    HealthCheckEndpoints.endpoints[F](HealthCheckService[F](), settings).orNotFound

  def start(settings: Settings): Stream[F, Server[F]] =
    BlazeServerBuilder[F]
      .bindHttp(settings.httpPort.value, settings.httpHost.value)
      .withHttpApp(endpoints(settings))
      .stream

}

object HttpServer {
  def apply[F[_]](implicit E: ConcurrentEffect[F], T: Timer[F]): HttpServer[F] = new HttpServer[F] {}
}
