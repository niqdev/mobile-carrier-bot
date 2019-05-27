package com.github.niqdev
package service

import java.time.ZonedDateTime
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.parser.decode

import cats.effect.Sync
import com.github.niqdev.model._

trait EventService[F[_]] {
  def getUser(id: Long): F[Option[InternalUser]]
}

object EventService extends EventServiceInstances {

  def apply[F[_]](implicit S: EventService[F]): EventService[F] = S
}

sealed trait EventServiceInstances {

  private[this] val events = List(
    Event(1, UserGroupEvent(), ZonedDateTime.now(), 888,
      UserCreated(888, name = "user1").asJson.noSpaces),
    Event(1, UserGroupEvent(), ZonedDateTime.now(), 888,
      UserInfoUpdated(name = "user1Updated").asJson.noSpaces),
    Event(1, UserGroupEvent(), ZonedDateTime.now(), 888,
      UserPhoneAdded(Phone("123", MobileNetworkOperator.ThreeIe, "usr1", "pwd1")).asJson.noSpaces),
    Event(1, UserGroupEvent(), ZonedDateTime.now(), 888,
      UserPhoneAdded(Phone("456", MobileNetworkOperator.TimIt, "usr2", "pwd2")).asJson.noSpaces),
    Event(1, UserGroupEvent(), ZonedDateTime.now(), 888,
      UserPhoneDeleted(Phone("456", MobileNetworkOperator.TimIt, "usr2", "pwd2")).asJson.noSpaces),
    Event(1, UserGroupEvent(), ZonedDateTime.now(), 888,
      UserInfoUpdated(name = "myUser1").asJson.noSpaces)
  )

  implicit def eventService[F[_]: Sync]: EventService[F] =
    id => Sync[F].delay {
      // TODO enumeratum or ADT .filter(_.eventGroup == UserGroupEvent()) ???
      // TODO lenses
      events
        .filter(_.userId == id)
        .map(event => decode[UserEvent](event.data))
        .foldLeft(Option.empty[InternalUser])((maybeUser, event) => {
          maybeUser.flatMap(tmpUser => event match {
            case Right(UserCreated(_, name)) =>
              Some(tmpUser.copy(name = Some(name)))
            // TODO
          })
        })
    }

}
