package com.github.niqdev
package model

import io.circe.Json
import io.circe.parser.parse
import org.scalatest.{Matchers, WordSpecLike}

final class TelegramModelSpec extends WordSpecLike with Matchers {

  private[this] def result(json: Json): Vector[Json] =
    json.hcursor
      .downField("result")
      .focus
      .flatMap(_.asArray)
      .getOrElse(Vector.empty[Json])

  "TelegramModel" must {

    "parse json User with defaults" in {
      val json: Json = parse(
        """
          |{
          |  "id": 123456789,
          |  "is_bot": false,
          |  "first_name": "MyFirstName"
          |}
        """.stripMargin).getOrElse(Json.Null)

      val expectedUser = User(
        id = 123456789,
        isBot = false,
        firstName = "MyFirstName"
      )

      json.as[User] shouldBe Right(expectedUser)
    }

    "parse json User" in {
      val json: Json = parse(
        """
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

    "parse json Message and ignore extra fields" in {
      val messageId: Long = 42
      val date: Long = 1556288851
      val text = "The Answer to the Ultimate Question of Life, The Universe, and Everything."
      val user = User(
        id = 123456789,
        isBot = false,
        firstName = "MyFirstName"
      )

      val json = parse(
        s"""
           |{
           |  "chat": {
           |    "first_name": "MyFirstName",
           |    "id": 123456789,
           |    "type": "private"
           |  },
           |  "date": $date,
           |  "invalid_number": 123,
           |  "invalid_object": {
           |    "key": "value"
           |  },
           |  "invalid_array": [{
           |    "key": "value"
           |  }],
           |  "from": {
           |    "first_name": "${user.firstName}",
           |    "id": ${user.id},
           |    "is_bot": ${user.isBot},
           |    "language_code": "${user.languageCode}"
           |  },
           |  "message_id": $messageId,
           |  "text": "$text"
           |}
         """.stripMargin).getOrElse(Json.Null)

      val expectedMessage = Message(
        messageId,
        Some(user),
        date,
        Some(text)
      )

      json.as[Message] shouldBe Right(expectedMessage)
    }

    "parse json Response and ignore invalid messages" in {
      val json: Json = parse(
        """
          |{
          |  "ok": true,
          |  "result": [
          |    {
          |      "message": {
          |        "date": 1556273132,
          |        "message_id": 1,
          |        "text": "hello"
          |      },
          |      "update_id": 220544280
          |    },
          |    {
          |      "edited_message": {
          |        "date": 1556273133,
          |        "message_id": 2,
          |        "text": "bye"
          |      },
          |      "update_id": 220544281
          |    }
          |  ]
          |}
        """.stripMargin).getOrElse(Json.Null)

      val expectedResponse = Response(
        ok = true,
        result = Some(Vector(
          Update(
            id = 220544280,
            message = Message(
              id = 1,
              date = 1556273132,
              text = Some("hello")
            )
          )
        ))
      )
      json.as[Response[Vector[Update]]] shouldBe Right(expectedResponse)
    }

    /*
    "parse Message json" in {
      val json: Json = parse(
        """
          |{
          |  "ok": true,
          |  "result": [
          |    {
          |      "message": {
          |        "chat": {
          |          "first_name": "MyFirstName",
          |          "id": 123456789,
          |          "type": "private"
          |        },
          |        "date": 1556273132,
          |        "from": {
          |          "first_name": "MyFirstName",
          |          "id": 123456789,
          |          "is_bot": false,
          |          "language_code": "en"
          |        },
          |        "message_id": 16,
          |        "text": "hello"
          |      },
          |      "update_id": 220544280
          |    },
          |    {
          |      "message": {
          |        "chat": {
          |          "first_name": "MyFirstName",
          |          "id": 123456789,
          |          "type": "private"
          |        },
          |        "date": 1556287777,
          |        "from": {
          |          "first_name": "MyFirstName",
          |          "id": 123456789,
          |          "is_bot": false,
          |          "language_code": "en"
          |        },
          |        "message_id": 17,
          |        "text": "bye"
          |      },
          |      "update_id": 220544281
          |    },
          |    {
          |      "message": {
          |        "chat": {
          |          "first_name": "MyFirstName",
          |          "id": 123456789,
          |          "type": "private"
          |        },
          |        "date": 1556288180,
          |        "from": {
          |          "first_name": "MyFirstName",
          |          "id": 123456789,
          |          "is_bot": false,
          |          "language_code": "en"
          |        },
          |        "message_id": 18,
          |        "reply_to_message": {
          |          "chat": {
          |            "first_name": "MyFirstName",
          |            "id": 123456789,
          |            "type": "private"
          |          },
          |          "date": 1556287777,
          |          "from": {
          |            "first_name": "MyFirstName",
          |            "id": 123456789,
          |            "is_bot": false,
          |            "language_code": "en"
          |          },
          |          "message_id": 17,
          |          "text": "bye"
          |        },
          |        "text": "answer"
          |      },
          |      "update_id": 220544282
          |    },
          |    {
          |      "edited_message": {
          |        "chat": {
          |          "first_name": "MyFirstName",
          |          "id": 123456789,
          |          "type": "private"
          |        },
          |        "date": 1556287777,
          |        "edit_date": 1556288198,
          |        "from": {
          |          "first_name": "MyFirstName",
          |          "id": 123456789,
          |          "is_bot": false,
          |          "language_code": "en"
          |        },
          |        "message_id": 17,
          |        "text": "edit"
          |      },
          |      "update_id": 220544283
          |    },
          |    {
          |      "message": {
          |        "chat": {
          |          "first_name": "MyFirstName",
          |          "id": 123456789,
          |          "type": "private"
          |        },
          |        "date": 1556288310,
          |        "from": {
          |          "first_name": "MyFirstName",
          |          "id": 123456789,
          |          "is_bot": false,
          |          "language_code": "en"
          |        },
          |        "message_id": 19,
          |        "reply_to_message": {
          |          "chat": {
          |            "first_name": "MyFirstName",
          |            "id": 123456789,
          |            "type": "private"
          |          },
          |          "date": 1556287777,
          |          "edit_date": 1556288198,
          |          "from": {
          |            "first_name": "MyFirstName",
          |            "id": 123456789,
          |            "is_bot": false,
          |            "language_code": "en"
          |          },
          |          "message_id": 17,
          |          "text": "edit"
          |        },
          |        "text": "reply_edit"
          |      },
          |      "update_id": 220544284
          |    },
          |    {
          |      "message": {
          |        "chat": {
          |          "first_name": "MyFirstName",
          |          "id": 123456789,
          |          "type": "private"
          |        },
          |        "date": 1556288650,
          |        "entities": [
          |          {
          |            "length": 6,
          |            "offset": 0,
          |            "type": "bot_command"
          |          }
          |        ],
          |        "from": {
          |          "first_name": "MyFirstName",
          |          "id": 123456789,
          |          "is_bot": false,
          |          "language_code": "en"
          |        },
          |        "message_id": 20,
          |        "text": "/start"
          |      },
          |      "update_id": 220544285
          |    },
          |    {
          |      "message": {
          |        "chat": {
          |          "first_name": "MyFirstName",
          |          "id": 123456789,
          |          "type": "private"
          |        },
          |        "date": 1556288851,
          |        "forward_date": 1554047812,
          |        "forward_from": {
          |          "first_name": "ForwardFirstName",
          |          "id": 987654321,
          |          "is_bot": false,
          |          "username": "ForwardUsername"
          |        },
          |        "from": {
          |          "first_name": "MyFirstName",
          |          "id": 123456789,
          |          "is_bot": false,
          |          "language_code": "en"
          |        },
          |        "message_id": 21,
          |        "text": "forward"
          |      },
          |      "update_id": 220544286
          |    }
          |  ]
          |}
        """.stripMargin).getOrElse(Json.Null)


    }

     */
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
          |          "first_name": "MyFirstName",
          |          "id": 123456789,
          |          "type": "private"
          |        },
          |        "date": 1556224743,
          |        "from": {
          |          "first_name": "MyFirstName",
          |          "id": 123456789,
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
          |          "first_name": "MyFirstName",
          |          "id": 123456789,
          |          "type": "private"
          |        },
          |        "date": 1556224743,
          |        "from": {
          |          "first_name": "MyFirstName",
          |          "id": 123456789,
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
          |          "first_name": "MyFirstName",
          |          "id": 123456789,
          |          "type": "private"
          |        },
          |        "date": 1556224868,
          |        "from": {
          |          "first_name": "MyFirstName",
          |          "id": 123456789,
          |          "is_bot": false,
          |          "language_code": "en"
          |        },
          |        "message_id": 15,
          |        "reply_to_message": {
          |          "chat": {
          |            "first_name": "MyFirstName",
          |            "id": 123456789,
          |            "type": "private"
          |          },
          |          "date": 1556224743,
          |          "from": {
          |            "first_name": "MyFirstName",
          |            "id": 123456789,
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
