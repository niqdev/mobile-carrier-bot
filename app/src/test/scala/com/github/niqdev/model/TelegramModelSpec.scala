package com.github.niqdev
package model

import io.circe.Json
import io.circe.parser.parse
import org.scalatest.{ Matchers, WordSpecLike }

final class TelegramModelSpec extends WordSpecLike with Matchers {

  "TelegramModel" must {

    "parse User json with defaults" in {
      val json: Json = parse("""
          |{
          |  "id": 123456789,
          |  "is_bot": false,
          |  "first_name": "MyFirstName"
          |}
        """.stripMargin).getOrElse(Json.Null)

      val expectedUser = User(
        id = 123456789,
        firstName = "MyFirstName"
      )

      json.as[User] shouldBe Right(expectedUser)
    }

    "parse User json" in {
      val json: Json = parse("""
          |{
          |  "id": 123456789,
          |  "is_bot": true,
          |  "first_name": "MyFirstName",
          |  "last_name": "MyLastName",
          |  "username": "MyUsername",
          |  "language_code": "en"
          |}
        """.stripMargin).getOrElse(Json.Null)

      val expectedUser = User(
        id = 123456789,
        isBot = true,
        firstName = "MyFirstName",
        lastName = Some("MyLastName"),
        username = Some("MyUsername"),
        languageCode = Some("en")
      )

      json.as[User] shouldBe Right(expectedUser)
    }
  }

  /*
  private[this] def result(json: Json): Vector[Json] =
    json.hcursor
      .downField("result")
      .focus
      .flatMap(_.asArray)
      .getOrElse(Vector.empty[Json])

  "Json test" must {

    "verify no messages" in {
      val json: Json = parse("""
          |{
          |  "ok": true,
          |  "result": []
          |}
        """.stripMargin).getOrElse(Json.Null)

      result(json) shouldBe Vector.empty[Json]
    }

    "verify single message" in {
      val json: Json = parse("""
          |{
          |  "ok": true,
          |  "result": [
          |    {
          |      "message": {
          |        "chat": {
          |          "first_name": "NiK",
          |          "id": 220095858,
          |          "type": "private"
          |        },
          |        "date": 1556224743,
          |        "from": {
          |          "first_name": "NiK",
          |          "id": 220095858,
          |          "is_bot": false,
          |          "language_code": "en"
          |        },
          |        "message_id": 13,
          |        "text": "aaa"
          |      },
          |      "update_id": 220544277
          |    }
          |  ]
          |}
        """.stripMargin).getOrElse(Json.Null)

      val messagesJson = result(json)

      // TODO validatedNel
      val messages: Vector[Either[DecodingFailure, Message]] =
        messagesJson.map { json =>
          val eitherMessage = for {
            from <- json.hcursor.downField("message").get[Chat]("from")
            text <- json.hcursor.downField("message").get[String]("text")
          } yield Message(from, text)

          eitherMessage
        }

      messages shouldBe ""
    }

    "verify messages of different format" in {
      val json: Json = parse("""
          |{
          |  "ok": true,
          |  "result": [
          |    {
          |      "message": {
          |        "chat": {
          |          "first_name": "NiK",
          |          "id": 220095858,
          |          "type": "private"
          |        },
          |        "date": 1556224743,
          |        "from": {
          |          "first_name": "NiK",
          |          "id": 220095858,
          |          "is_bot": false,
          |          "language_code": "en"
          |        },
          |        "message_id": 13,
          |        "text": "nik"
          |      },
          |      "update_id": 220544277
          |    },
          |    {
          |      "message": {
          |        "chat": {
          |          "first_name": "NiK",
          |          "id": 220095858,
          |          "type": "private"
          |        },
          |        "date": 1556224868,
          |        "from": {
          |          "first_name": "NiK",
          |          "id": 220095858,
          |          "is_bot": false,
          |          "language_code": "en"
          |        },
          |        "message_id": 15,
          |        "reply_to_message": {
          |          "chat": {
          |            "first_name": "NiK",
          |            "id": 220095858,
          |            "type": "private"
          |          },
          |          "date": 1556224743,
          |          "from": {
          |            "first_name": "NiK",
          |            "id": 220095858,
          |            "is_bot": false,
          |            "language_code": "en"
          |          },
          |          "message_id": 13,
          |          "text": "nik"
          |        },
          |        "text": "gg"
          |      },
          |      "update_id": 220544279
          |    }
          |  ]
          |}
        """.stripMargin).getOrElse(Json.Null)

      result(json) shouldBe Vector.empty[Json]
    }
  }
 */

}
