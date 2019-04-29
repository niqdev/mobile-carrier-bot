package com.github.niqdev
package http

import cats.effect.{ ConcurrentEffect, Resource, Timer }
import com.github.niqdev.model.Settings
import com.github.niqdev.service.HealthCheckService
import org.http4s.HttpRoutes
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.server.Server
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli.http4sKleisliResponseSyntax

import scala.concurrent.ExecutionContext

sealed abstract class HttpResource[F[_]: ConcurrentEffect: Timer] {

  private[http] def endpoints(settings: Settings): HttpRoutes[F] =
    HealthCheckEndpoints[F].endpoints(HealthCheckService(), settings)

  // TODO middleware
  // .withHttpApp(Logger.httpApp(logHeaders = true, logBody = true)(endpoints(settings).orNotFound))
  // https://http4s.org/v0.20/middleware
  // https://github.com/ChristopherDavenport/http4s-resource-example/blob/master/src/main/scala/org/http4s/http4sresourceexample/Main.scala#L52
  def server(settings: Settings): Resource[F, Server[F]] =
    BlazeServerBuilder[F]
      .bindHttp(settings.server.port.value, settings.server.host.value)
      .withHttpApp(endpoints(settings).orNotFound)
      .resource

  def client(executionContext: ExecutionContext): Resource[F, Client[F]] =
    BlazeClientBuilder[F](executionContext).resource
}

object HttpResource {
  def apply[F[_]: ConcurrentEffect: Timer]: HttpResource[F] =
    new HttpResource[F] {}
}
