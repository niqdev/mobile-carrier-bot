package com.github.niqdev
package model

import java.time.ZonedDateTime

final case class Event(
  eventId: Long,
  eventGroup: EventGroup,
  eventType: String,
  timestamp: ZonedDateTime,
  userId: Long,
  data: String
)

sealed trait EventGroup
case object UserGroupEvent extends EventGroup
case object ConversationGroupEvent extends EventGroup

sealed trait UserEvent
// TODO pure UUID
final case class UserCreated(id: Long, name: String) extends UserEvent
final case class UserDeleted(id: Long) extends UserEvent
final case class UserInfoUpdated(name: String) extends UserEvent
final case class UserPhoneAdded(phone: Phone) extends UserEvent
final case class UserPhoneUpdated(phone: Phone) extends UserEvent
final case class UserPhoneDeleted(phone: Phone) extends UserEvent

// TODO rename TelegramUser and this User
final case class InternalUser(
  id: Option[String] = None,
  name: Option[String] = None,
  phones: List[Phone] = List.empty[Phone],
  lastUpdate: Option[ZonedDateTime] = None
)

final case class Phone(
  number: String,
  operator: MobileNetworkOperator,
  username: String,
  password: String
)
