package net.benlamlih
package domain.reservation

import domain.account.UUID

import cats.effect.IO

trait ReservationRepository {
  def create(reservation: Reservation): IO[Unit]
  def find(id: UUID): IO[Option[Reservation]]
  def findAll(): IO[List[Reservation]]
  def findByAccount(accountId: UUID): IO[List[Reservation]]
  def update(reservation: Reservation): IO[Unit]
  def delete(id: UUID): IO[Unit]
}
