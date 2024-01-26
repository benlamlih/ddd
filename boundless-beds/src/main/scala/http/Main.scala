package net.benlamlih
package http

import database.account.AccountRepositoryImpl
import domain.account.{AccountRepository, AccountService}
import domain.wallet.WalletService
import http.Server

import cats.effect.{ExitCode, IO, IOApp}
import doobie.Transactor
import doobie.util.log
import doobie.util.log.LogHandler
import doobie.util.transactor.Transactor.Aux
import net.benlamlih.database.reservation.ReservationRepositoryImpl
import net.benlamlih.domain.reservation.{ReservationRepository, ReservationService}

object Main extends IOApp {

  val xa: Aux[IO, Unit]   = Transactor.fromDriverManager[IO](
    driver = "org.postgresql.Driver",
    url = "jdbc:postgresql://localhost:5432/boundless-beds",
    user = "user",
    password = "password",
    logHandler = Some(
      new LogHandler[IO] {
        override def run(logEvent: log.LogEvent): IO[Unit] = IO { println(logEvent.sql) }
      }
    )
  )
  def run(args: List[String]): IO[ExitCode] = {

    val accountRepository: AccountRepository         = new AccountRepositoryImpl(xa)
    val reservationRepository: ReservationRepository = new ReservationRepositoryImpl(xa)
    val accountService: AccountService               = new AccountService(accountRepository)
    val walletService: WalletService                 = new WalletService(accountRepository, accountService)
    val reservationService: ReservationService       = new ReservationService(reservationRepository, walletService)

    val server = new Server(accountService, walletService, reservationService)

    server.run()
  }
}
