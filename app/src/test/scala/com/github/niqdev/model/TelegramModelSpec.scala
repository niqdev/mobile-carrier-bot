package com.github.niqdev
package model

import io.circe.Json
import io.circe.parser.parse
import org.scalatest.{ Matchers, WordSpecLike }

final class TelegramModelSpec extends WordSpecLike with Matchers {

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
      val date: Long      = 1556288851
      val text            = "The Answer to the Ultimate Question of Life, The Universe, and Everything."
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
        result = Some(
          Vector(
            Update(
              id = 220544280,
              message = Some(
                Message(
                  id = 1,
                  date = 1556273132,
                  text = Some("hello")
                )
              )
            ),
            Update(
              id = 220544281,
              message = None
            )
          )
        )
      )
      json.as[Response[Vector[Update]]] shouldBe Right(expectedResponse)
    }

  }
}
