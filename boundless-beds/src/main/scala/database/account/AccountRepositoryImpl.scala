package net.benlamlih
package database.account

import database.account.AccountMeta.*
import domain.account.{Account, AccountRepository, Currency, UUID}

import cats.effect.IO
import doobie.implicits.*
import doobie.refined.*
import doobie.refined.implicits.*
import doobie.util.transactor.Transactor
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.Uuid

class AccountRepositoryImpl(xa: Transactor[IO]) extends AccountRepository {

  override def create(account: Account): IO[Unit] =
    sql"insert into account (id, full_name, email, phone_number, balance, currency) values (${account.id}::uuid, ${account.fullName}, ${account.email}, ${account.phoneNumber}, ${account.balance}, ${account.currency}::currency_type)".update.run
      .transact(xa)
      .map(_ => ())

  override def find(name: String): IO[Option[Account]] =
    sql"select * from account where full_name = $name".query[Account].option.transact(xa)

  override def retrieve(id: UUID): IO[Option[Account]] = {
    println("before executing request")
    sql"select * from account where id = ${id}::uuid".query[Account].option.transact(xa)
  }

  override def retrieveAll(): IO[List[Account]] = {
    sql"SELECT * FROM account"
      .query[Account]
      .to[List]
      .transact(xa)
  }

  override def update(account: Account): IO[Unit] =
    sql"update account set full_name = ${account.fullName}, email = ${account.email}, phone_number = ${account.phoneNumber}, balance = ${account.balance}, currency = ${account.currency}::currency_type where id = ${account.id}::uuid".update.run
      .transact(xa)
      .map(_ => ())

  override def delete(id: UUID): IO[Unit] =
    sql"delete from account where id = ${id}::uuid".update.run
      .transact(xa)
      .map(_ => ())
}
