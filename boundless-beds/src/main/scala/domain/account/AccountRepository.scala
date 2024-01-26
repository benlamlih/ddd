package net.benlamlih
package domain.account

import cats.effect.IO

trait AccountRepository {
  def create(account: Account): IO[Unit]
  def find(name: String): IO[Option[Account]]
  def retrieve(id: UUID): IO[Option[Account]]
  def retrieveAll(): IO[List[Account]]
  def update(account: Account): IO[Unit]
  def delete(id: UUID): IO[Unit]
}
