package net.benlamlih
package domain.reservation

import domain.account.UUID
import domain.room.RoomType

import doobie.refined.*
import doobie.util.meta.Meta
import doobie.{Get, Put}
import eu.timepit.refined.*
import eu.timepit.refined.api.Refined
import eu.timepit.refined.boolean.AllOf
import eu.timepit.refined.numeric.Greater
import eu.timepit.refined.string.*
import eu.timepit.refined.types.string.NonEmptyString

import java.time.LocalDate

case class Reservation(
    id: UUID,
    accountId: UUID,
    roomType: RoomType,
    checkInDate: LocalDate,
    checkOutDate: LocalDate,
    totalPrice: BigDecimal,
    isConfirmed: Boolean
)

object Reservation {
  def create(
      id: UUID,
      accountId: UUID,
      roomType: RoomType,
      checkInDate: LocalDate,
      checkOutDate: LocalDate,
      totalPrice: BigDecimal,
      isConfirmed: Boolean
  ): Either[String, Reservation] = {
    if (!checkOutDate.isAfter(checkInDate)) {
      Left("Check-out date must be after check-in date.")
    } else {
      Right(new Reservation(id, accountId, roomType, checkInDate, checkOutDate, totalPrice, isConfirmed = false))
    }
  }
}
