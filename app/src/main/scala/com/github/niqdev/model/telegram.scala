package com.github.niqdev
package model

import cats.Applicative
import cats.effect.Sync
import enumeratum.{ Enum, EnumEntry }
import io.circe.generic.extras.{ AutoDerivation, Configuration, ConfiguredJsonCodec }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import io.circe.{ Decoder, Encoder, HCursor, Json }
import org.http4s.circe.{ jsonEncoderOf, jsonOf }
import org.http4s.{ EntityDecoder, EntityEncoder }

// https://circe.github.io/circe/codecs/custom-codecs.html#custom-key-mappings-via-annotations
// https://github.com/circe/circe/blob/master/modules/generic-extras/src/test/scala/io/circe/generic/extras/ConfiguredJsonCodecWithKeySuite.scala
private[model] sealed trait CirceSnakeCaseConfiguration extends AutoDerivation {

  implicit val snakeCase: Configuration =
    Configuration.default.withSnakeCaseMemberNames
}

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
@ConfiguredJsonCodec
final case class User(
  id: Long,
  isBot: Boolean,
  firstName: String,
  lastName: Option[String] = None,
  username: Option[String] = None,
  languageCode: Option[String] = None
)

object User extends CirceSnakeCaseConfiguration

// TODO refined + date (Instant or ZonedDateTime)
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

  import io.circe.syntax.EncoderOps

  implicit val messageEncoder: Encoder[Message] =
    (message: Message) =>
      Json.obj(
        ("message_id", Json.fromLong(message.id)),
        ("from", message.from.asJson),
        ("date", message.date.asJson),
        ("text", message.text.asJson)
      )

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

  implicit val updateEncoder: Encoder[Update] =
    Encoder.forProduct2("update_id", "message") { update =>
      (update.id, update.message)
    }

  implicit val updateDecoder: Decoder[Update] =
    Decoder.forProduct2("update_id", "message")(Update.apply)

  implicit val orderByUpdateId: Ordering[Update] =
    Ordering.by(_.id)
}

/**
  * [[https://core.telegram.org/bots/api#responseparameters ResponseParameters]]
  *
  * Contains information about why a request was unsuccessful.
  */
@ConfiguredJsonCodec
final case class ResponseParameters(
  migrateToChatId: Long,
  retryAfter: Long
)

object ResponseParameters extends CirceSnakeCaseConfiguration

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

  implicit def responseEncoder[T: Encoder]: Encoder[Response[T]] =
    deriveEncoder[Response[T]]

  implicit def responseEntityEncoder[F[_]: Applicative]: EntityEncoder[F, Response[List[Update]]] =
    jsonEncoderOf[F, Response[List[Update]]]

  implicit def responseDecoder[T: Decoder]: Decoder[Response[T]] =
    deriveDecoder[Response[T]]

  implicit def responseEntityDecoder[F[_]: Sync]: EntityDecoder[F, Response[List[Update]]] =
    jsonOf[F, Response[List[Update]]]

  implicit def responseMessage[F[_]: Sync]: EntityDecoder[F, Response[Message]] =
    jsonOf[F, Response[Message]]
}

// TODO parse_mode, reply_markup
@ConfiguredJsonCodec
final case class SendMessage(
  chatId: String,
  text: String
)

object SendMessage extends CirceSnakeCaseConfiguration {

  // chat_id: Integer or String
  def apply(chatId: Long, text: String): SendMessage =
    SendMessage(s"$chatId", text)

  implicit def settingsEntityEncoder[F[_]: Applicative]: EntityEncoder[F, SendMessage] =
    jsonEncoderOf[F, SendMessage]
}

/**
  * [[https://core.telegram.org/bots#global-commands BotCommand]]
  */
sealed abstract class BotCommand(override val entryName: String) extends EnumEntry

object BotCommand extends Enum[BotCommand] {

  // macro
  val values = findValues

  case object Start extends BotCommand("/start")
  case object Help  extends BotCommand("/help")

  def parseCommand(value: String): String =
    withNameOption(value) match {
      case Some(Start) =>
        "TODO start"
      case Some(Help) =>
        "TODO help"
      case _ =>
        "unknown command"
    }
}
