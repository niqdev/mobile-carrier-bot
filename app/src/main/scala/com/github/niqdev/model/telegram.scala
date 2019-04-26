package com.github.niqdev
package model

import io.circe.Decoder

// TODO refined + enumeratum
// TODO validation: ignore bot, default languageCode
/**
  * [[https://core.telegram.org/bots/api#user User]]
  * [[https://en.wikipedia.org/wiki/IETF_language_tag IETF language tag]]
  *
  * @param id Unique identifier for this user or bot
  * @param isBot True, if this user is a bot
  * @param firstName User‘s or bot’s first name
  * @param lastName Optional. User‘s or bot’s last name
  * @param username Optional. User‘s or bot’s username
  * @param languageCode Optional. IETF language tag of the user's language
  */
final case class User(
  id: Long,
  isBot: Boolean = false,
  firstName: String,
  lastName: Option[String] = None,
  username: Option[String] = None,
  languageCode: Option[String] = None
)

object User {

  implicit val userDecoder: Decoder[User] =
    Decoder.forProduct6(
      "id",
      "is_bot",
      "first_name",
      "last_name",
      "username",
      "language_code")(User.apply)
}

// TODO refined + date
// TODO validation: ignore if user or text is None
/**
  * [[https://core.telegram.org/bots/api#message Message]]
  *
  * @param id Unique message identifier inside this chat
  * @param from Optional. Sender, empty for messages sent to channels
  * @param date Date the message was sent in Unix time
  * @param text Optional. For text messages, the actual UTF-8 text of the message, 0-4096 characters.
  */
final case class Message(
  id: Long,
  from: Option[User],
  date: Long,
  text: Option[String]
)
