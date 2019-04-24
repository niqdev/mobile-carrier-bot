package com.github.niqdev
package service

import cats.effect.Sync
import cats.implicits.catsSyntaxTuple2Semigroupal
import com.github.niqdev.algebra.MobileCarrierClient
import com.github.niqdev.model.MobileNetworkOperator.{ThreeIe, TimIt}

/*
 * TODO
 * parMapN Sync instead of IO https://typelevel.org/cats-effect/datatypes/io.html#parmapn
 * ValidateNel: accumulate errors
 *
 * tests ???
 */
object MobileCarrierService {

  def retrieveBalances[F[_]: Sync]: F[String] =
    (
      MobileCarrierClient[F, ThreeIe].balance("", ""),
      MobileCarrierClient[F, TimIt].balance("", "")
    ).mapN((threeBalance, timBalance) => s"Balances: [Three=$threeBalance][Tim=$timBalance]")

}
