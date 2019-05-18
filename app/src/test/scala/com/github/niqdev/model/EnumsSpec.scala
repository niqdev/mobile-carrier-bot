package com.github.niqdev
package model

import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import io.circe.{ Decoder, Encoder }

private[this] final case class ExampleAuto(environment: Environment)
private[this] final case class ExampleSemiAuto(driver: DatabaseDriver)

private[this] object ExampleSemiAuto {

  implicit val exampleEncoder: Encoder[ExampleSemiAuto] =
    deriveEncoder[ExampleSemiAuto]

  implicit val exampleDecoder: Decoder[ExampleSemiAuto] =
    deriveDecoder[ExampleSemiAuto]
}

final class EnumsSpec extends BaseSpec {

  "Environment" must {

    "verify enum" in {
      Environment.values.length shouldBe 3

      Environment.Local.entryName shouldBe "LOCAL"
      Environment.Development.entryName shouldBe "DEVELOPMENT"
      Environment.Production.entryName shouldBe "PRODUCTION"
    }

    "verify encoder" in {
      import io.circe.generic.auto.exportEncoder
      import io.circe.syntax.EncoderOps

      val expectedResult = """{"environment":"LOCAL"}"""
      ExampleAuto(Environment.Local).asJson.noSpaces shouldBe expectedResult
    }

    "verify decoder" in {
      import io.circe.generic.auto.exportDecoder
      import io.circe.parser.decode

      val expectedResult = ExampleAuto(Environment.Development)
      decode[ExampleAuto]("""{"environment":"DEVELOPMENT"}""") shouldBe Right(expectedResult)
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

    "verify encoder" in {
      import io.circe.syntax.EncoderOps

      val expectedResult = """{"driver":"org.postgresql.Driver"}"""
      ExampleSemiAuto(DatabaseDriver.PostgreSQL).asJson.noSpaces shouldBe expectedResult
    }

    "verify decoder" in {
      import io.circe.parser.decode

      val expectedResult = ExampleSemiAuto(DatabaseDriver.Cache)
      decode[ExampleSemiAuto]("""{"driver":"cats.effect.concurrent.Ref"}""") shouldBe Right(expectedResult)
    }
  }

}
