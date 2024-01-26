package net.benlamlih
package domain.account

import cats.effect.IO
import eu.timepit.refined.api.Refined
import eu.timepit.refined.types.all.NonEmptyString

import java.util.UUID as JavaUUID

class AccountService(repository: AccountRepository) {

  def createAccount(
      fullName: NonEmptyString,
      email: Email,
      phoneNumber: PhoneNumber,
      balance: BigDecimal = BigDecimal(0),
      currency: Currency = Currency.EUR
  ): IO[Account] = {
    val account = Account(
      Refined.unsafeApply(JavaUUID.randomUUID().toString),
      fullName,
      email,
      phoneNumber,
      balance,
      Currency.EUR
    )
    repository.create(account).as(account)
  }

  def getAccount(id: UUID): IO[Option[Account]] = {
    repository.retrieve(id)
  }

  def getAllAccounts: IO[List[Account]] = {
    repository.retrieveAll()
  }

  def getBalance(id: UUID): IO[BigDecimal] = {
    repository.retrieve(id).flatMap {
      case Some(account) => IO.pure(account.balance)
      case None          => IO.raiseError(new Exception("Account not found"))
    }
  }

  def updateAccount(
      id: UUID,
      fullName: NonEmptyString,
      email: Email,
      phoneNumber: PhoneNumber,
      balance: BigDecimal,
      currency: Currency
  ): IO[Option[Account]] = {
    // Should remove the currency from the db, but for now just force it to also be Euro!
    val account = Account(id, fullName, email, phoneNumber, balance, Currency.EUR)
    repository.update(account).map(_ => Some(account))
  }

  def deleteAccount(id: UUID): IO[Unit] = {
    repository.delete(id).map(_ => ())
  }
}
