package com.github.niqdev
package service

import cats.effect.Sync
import com.github.niqdev.model.BuildInformation

sealed abstract class HealthCheckService[F[_]: Sync] {

  def buildInformation: F[BuildInformation] =
    Sync[F].pure {
      BuildInformation(
        BuildInfo.name,
        BuildInfo.version,
        BuildInfo.scalaVersion,
        BuildInfo.builtAtString
      )
    }
}

object HealthCheckService {

  def apply[F[_]: Sync](): HealthCheckService[F] =
    new HealthCheckService[F] {}
}
