package com.github.niqdev
package http

import cats.data.Kleisli
import cats.effect.{ ConcurrentEffect, Resource, Timer }
import com.github.niqdev.model.Settings
import com.github.niqdev.service.HealthCheckService
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.server.Server
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli.http4sKleisliResponseSyntax
import org.http4s.{ Request, Response }

import scala.concurrent.ExecutionContext

sealed abstract class Http[F[_]](implicit E: ConcurrentEffect[F], T: Timer[F]) {

  private[http] def endpoints(settings: Settings): Kleisli[F, Request[F], Response[F]] =
    HealthCheckEndpoints.endpoints[F](HealthCheckService[F](), settings).orNotFound

  def server(settings: Settings): Resource[F, Server[F]] =
    BlazeServerBuilder[F]
      .bindHttp(settings.httpPort.value, settings.httpHost.value)
      .withHttpApp(endpoints(settings))
      .resource

  def client(implicit executionContext: ExecutionContext): Resource[F, Client[F]] =
    BlazeClientBuilder[F](executionContext).resource

}

object Http {
  def apply[F[_]](implicit E: ConcurrentEffect[F], T: Timer[F]): Http[F] = new Http[F] {}
}
