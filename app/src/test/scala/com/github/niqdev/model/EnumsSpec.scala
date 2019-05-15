package com.github.niqdev
package model

final class EnumsSpec extends BaseSpec {

  "Environment" must {

    "verify enum" in {
      Environment.values.length shouldBe 3

      Environment.Local.entryName shouldBe "LOCAL"
      Environment.Development.entryName shouldBe "DEVELOPMENT"
      Environment.Production.entryName shouldBe "PRODUCTION"
    }
  }

  "LogLevel" must {

    "verify enum" in {
      LogLevel.values.length shouldBe 4

      LogLevel.Debug.entryName shouldBe "DEBUG"
      LogLevel.Info.entryName shouldBe "INFO"
      LogLevel.Warn.entryName shouldBe "WARN"
      LogLevel.Error.entryName shouldBe "ERROR"
    }
  }

  "DatabaseDriver" must {

    "verify enum" in {
      DatabaseDriver.values.length shouldBe 3

      DatabaseDriver.PostgreSQL.entryName shouldBe "postgresql"
      DatabaseDriver.PostgreSQL.className.value shouldBe "org.postgresql.Driver"

      DatabaseDriver.H2.entryName shouldBe "h2"
      DatabaseDriver.H2.className.value shouldBe "org.h2.Driver"

      DatabaseDriver.Cache.entryName shouldBe "cache"
      DatabaseDriver.Cache.className.value shouldBe "cats.effect.concurrent.Ref"
    }
  }

}
