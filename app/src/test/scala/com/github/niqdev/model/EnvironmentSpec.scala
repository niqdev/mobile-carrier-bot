package com.github.niqdev.model

import com.github.niqdev.BaseSpec

final class EnvironmentSpec extends BaseSpec {

  "Environment" must {

    "verify enum" in {
      Environment.Local.entryName shouldBe "LOCAL"
      Environment.Development.entryName shouldBe "DEVELOPMENT"
      Environment.Production.entryName shouldBe "PRODUCTION"
    }
  }

}
