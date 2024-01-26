package net.benlamlih
package domain.wallet

import domain.account.*

import cats.effect.IO

class WalletService(accountRepository: AccountRepository, accountService: AccountService) {

  def fundWallet(accountId: UUID, amount: BigDecimal, currency: Currency): IO[Account] = {
    val amountInEUR = convertToEUR(amount, currency)
    for {
      currentAccount <- accountRepository.retrieve(accountId).flatMap {
                          case Some(account) => IO.pure(account)
                          case None          => IO.raiseError(new Exception("Account not found"))
                        }
      updatedAccount  = currentAccount.copy(balance = currentAccount.balance + amountInEUR)
      _              <- accountRepository.update(updatedAccount)
    } yield updatedAccount
  }

  def convertToEUR(amount: BigDecimal, currency: Currency): BigDecimal = {
    val conversionRate = currency match {
      case Currency.EUR => 1.0
      case Currency.USD => 0.89
      case Currency.GBP => 1.17
      case Currency.JPY => 0.0074
      case Currency.CHF => 0.95
    }
    amount * BigDecimal(conversionRate)
  }

  def checkBalance(accountId: UUID, amountRequired: BigDecimal): IO[Unit] = {
    accountService.getBalance(accountId).flatMap { balance =>
      if (balance >= amountRequired) IO.unit
      else IO.raiseError(new Exception("Insufficient balance"))
    }
  }

  def deductFromBalance(accountId: UUID, amount: BigDecimal): IO[Unit] = {
    for {
      currentBalance <- accountService.getBalance(accountId)
      _              <- checkBalance(accountId, amount)
      newBalance      = currentBalance - amount
      _              <- updateBalance(accountId, newBalance)
    } yield ()
  }

  private def updateBalance(accountId: UUID, newBalance: BigDecimal): IO[Unit] = {
    accountRepository.retrieve(accountId).flatMap {
      case Some(account) =>
        val updatedAccount = account.copy(balance = newBalance)
        accountRepository.update(updatedAccount)
      case None          => IO.raiseError(new Exception("Account not found"))
    }
  }

}
