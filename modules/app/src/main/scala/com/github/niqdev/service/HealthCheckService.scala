package com.github.niqdev
package service

import cats.effect.Sync
import com.github.niqdev.model.BuildInformation

sealed abstract class HealthCheckService {

  def buildInformation[F[_]: Sync]: F[BuildInformation] =
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
  def apply(): HealthCheckService =
    new HealthCheckService {}
}
