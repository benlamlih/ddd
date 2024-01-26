package net.benlamlih
package database.reservation

import domain.room.RoomType

import doobie.Get
import doobie.Put

object ReservationMeta {
  implicit val roomTypeGet: Get[RoomType] = Get[String].temap(RoomType.fromString)
  implicit val roomTypePut: Put[RoomType] = Put[String].contramap(RoomType.asString)
}
