package com.github.niqdev
package model

import io.circe.parser.parse
import io.circe.{ Decoder, DecodingFailure, Json }
import org.scalatest.{ Matchers, WordSpecLike }

// TODO refined + enumeratum (check Long)
// TODO filter is_bot
final case class Chat(id: Long, firstName: String, language: String)
// TODO message_id, date and update_id + make fields optional
// TODO strict only on required fields, ignore everything else
// https://core.telegram.org/bots/api#message
final case class Message(chat: Chat, text: String)

final class JsonSpec extends WordSpecLike with Matchers {

  implicit val chatDecoder: Decoder[Chat] =
    Decoder.forProduct3("id", "first_name", "language_code")(Chat.apply)

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

}
