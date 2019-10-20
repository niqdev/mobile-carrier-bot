package com.github.niqdev
package model

import com.github.niqdev.model.MobileNetworkOperator.{ ThreeIe, TimIt }

final class MobileNetworkOperatorSpec extends BaseSpec {

  "MobileNetworkOperator" must {

    "verify enum" in {
      ThreeIe.entryName shouldBe "THREE_IE"
      TimIt.entryName shouldBe "TIM_IT"
    }
  }

}
