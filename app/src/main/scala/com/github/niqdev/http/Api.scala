package com.github.niqdev
package http

import cats.data.Kleisli
import cats.effect.Sync
import org.http4s.syntax.kleisli.http4sKleisliResponseSyntax
import org.http4s.{Request, Response}

object Api {

  def endpoints[F[_] : Sync]: Kleisli[F, Request[F], Response[F]] =
    HealthCheckEndpoint[F].routes.orNotFound

}
