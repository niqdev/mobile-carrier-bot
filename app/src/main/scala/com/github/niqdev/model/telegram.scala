package com.github.niqdev
package model

import cats.effect.Sync
import enumeratum.EnumEntry.{ Snakecase, Uppercase }
import io.circe.generic.semiauto.deriveDecoder
import io.circe.{ Decoder, HCursor }
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf
import enumeratum.{ Enum, EnumEntry }
import eu.timepit.refined.types.string.NonEmptyString
import eu.timepit.refined.auto.autoRefineV

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
  isBot: Boolean,
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
// TODO validation: ignore if user or text is None, dedup
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
  from: Option[User] = None,
  date: Long,
  text: Option[String] = None
)

object Message {

  implicit val messageDecoder: Decoder[Message] =
    (c: HCursor) =>
      for {
        id   <- c.downField("message_id").as[Long]
        from <- c.downField("from").as[Option[User]]
        date <- c.downField("date").as[Long]
        text <- c.downField("text").as[Option[String]]
      } yield Message(id, from, date, text)
}

/**
  * [[https://core.telegram.org/bots/api#update Update]]
  *
  * @param id The update‘s unique identifier
  * @param message Optional. New incoming message of any kind — text, photo, sticker, etc.
  */
final case class Update(id: Long, message: Option[Message])

object Update {

  implicit val updateDecoder: Decoder[Update] =
    Decoder.forProduct2(
      "update_id",
      "message")(Update.apply)

  implicit val orderByUpdateId: Ordering[Update] =
    Ordering.by(_.id)
}

/**
  * [[https://core.telegram.org/bots/api#responseparameters ResponseParameters]]
  *
  * Contains information about why a request was unsuccessful.
  */
final case class ResponseParameters(
  migrateToChatId: Long,
  retryAfter: Long
)

object ResponseParameters {

  implicit val responseParametersDecoder: Decoder[ResponseParameters] =
    Decoder.forProduct2(
      "migrate_to_chat_id",
      "retry_after")(ResponseParameters.apply)
}

/**
  * [[https://core.telegram.org/bots/api#making-requests Response]]
  */
final case class Response[T](
  ok: Boolean,
  description: Option[String] = None,
  result: Option[T] = None,
  errorCode: Option[Long] = None,
  parameters: Option[ResponseParameters] = None
)

object Response {

  implicit def responseDecoder[T: Decoder]: Decoder[Response[T]] =
    deriveDecoder[Response[T]]

  implicit def responseEntityDecoder[F[_]: Sync]: EntityDecoder[F, Response[Vector[Update]]] =
    jsonOf[F, Response[Vector[Update]]]
}

/**
  * [[https://core.telegram.org/bots#global-commands BotCommand]]
  */
sealed abstract class BotCommand(val command: NonEmptyString) extends EnumEntry with Snakecase with Uppercase

object BotCommand extends Enum[BotCommand] {

  // macro
  val values = findValues

  case object Start    extends BotCommand("/start")
  case object Help     extends BotCommand("/help")
  case object Settings extends BotCommand("/settings")
}
