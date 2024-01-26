package net.benlamlih
package database.reservation

import database.reservation.ReservationMeta.*
import domain.account.UUID
import domain.reservation.{Reservation, ReservationRepository}

import cats.effect.IO
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.refined.*
import doobie.refined.implicits.*
import doobie.util.transactor.Transactor
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.Uuid

class ReservationRepositoryImpl(xa: Transactor[IO]) extends ReservationRepository {
  override def create(reservation: Reservation): IO[Unit] =
    sql"insert into reservation (id, account_id, room_type, check_in_date, check_out_date, total_price, is_confirmed) values (${reservation.id}::uuid, ${reservation.accountId}::uuid, ${reservation.roomType.toString}::room_type, ${reservation.checkInDate}, ${reservation.checkOutDate}, ${reservation.totalPrice}, ${reservation.isConfirmed})".update.run
      .transact(xa)
      .map(_ => ())

  override def find(id: UUID): IO[Option[Reservation]] =
    sql"select * from reservation where id = ${id}::uuid"
      .query[Reservation]
      .option
      .transact(xa)

  override def findAll(): IO[List[Reservation]] =
    sql"select * from reservation"
      .query[Reservation]
      .to[List]
      .transact(xa)

  override def findByAccount(accountId: UUID): IO[List[Reservation]] =
    sql"select * from reservation where account_id = ${accountId}::uuid"
      .query[Reservation]
      .to[List]
      .transact(xa)

  override def update(reservation: Reservation): IO[Unit] =
    sql"update reservation set account_id = ${reservation.accountId}::uuid, room_type = ${reservation.roomType.toString}::room_type, check_in_date = ${reservation.checkInDate}, check_out_date = ${reservation.checkOutDate}, total_price = ${reservation.totalPrice}, is_confirmed = ${reservation.isConfirmed}::boolean where id = ${reservation.id}::uuid".update.run
      .transact(xa)
      .map(_ => ())

  override def delete(id: UUID): IO[Unit] =
    sql"delete from reservation where id = ${id}::uuid".update.run
      .transact(xa)
      .map(_ => ())
}
