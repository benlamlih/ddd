package net.benlamlih
package domain.reservation

import domain.account.{AccountService, UUID}
import domain.room.RoomType
import domain.wallet.WalletService

import cats.effect.IO
import eu.timepit.refined.api.Refined
import net.benlamlih.domain.account.Currency.EUR

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID as JavaUUID

class ReservationService(repository: ReservationRepository, walletService: WalletService) {

  // Once we are in the App, everything works in EURO!
  // Only funding the wallet is possible using other currencies, and after that everything is in EURO
  private[reservation] def calculateTotalPrice(roomType: RoomType, nights: Int): BigDecimal = roomType match {
    case RoomType.Standard => 50 * nights
    case RoomType.Superior => 100 * nights
    case RoomType.Suite    => 200 * nights
  }

  def createReservation(
      accountId: UUID,
      roomType: RoomType,
      checkInDate: LocalDate,
      checkOutDate: LocalDate
  ): IO[Reservation] = {
    val nights     = ChronoUnit.DAYS.between(checkInDate, checkOutDate).toInt
    val totalPrice = calculateTotalPrice(roomType, nights)
    val halfPrice  = totalPrice / 2

    for {
      _          <- walletService.checkBalance(accountId, halfPrice)
      _          <- walletService.deductFromBalance(accountId, halfPrice)
      reservation = Reservation(
                      Refined.unsafeApply(JavaUUID.randomUUID().toString),
                      accountId,
                      roomType,
                      checkInDate,
                      checkOutDate,
                      totalPrice,
                      isConfirmed = false
                    )
      _          <- repository.create(reservation)
    } yield reservation
  }

  def updateReservation(
      id: UUID,
      accountId: UUID,
      newCheckInDate: LocalDate,
      newCheckOutDate: LocalDate
  ): IO[Option[Reservation]] = {
    repository.find(id).flatMap {
      case Some(reservation) =>
        val nights          = ChronoUnit.DAYS.between(newCheckInDate, newCheckOutDate).toInt
        val newTotalPrice   = calculateTotalPrice(reservation.roomType, nights)
        val priceDifference = newTotalPrice - reservation.totalPrice

        if (priceDifference > 0) {
          for {
            _                 <- walletService.checkBalance(accountId, priceDifference)
            _                 <- walletService.deductFromBalance(accountId, priceDifference)
            updatedReservation = reservation.copy(
                                   checkInDate = newCheckInDate,
                                   checkOutDate = newCheckOutDate,
                                   totalPrice = newTotalPrice
                                 )
            _                 <- repository.update(updatedReservation)
          } yield Some(updatedReservation)
        } else {
          val updatedReservation = reservation.copy(
            checkInDate = newCheckInDate,
            checkOutDate = newCheckOutDate,
            totalPrice = newTotalPrice
          )
          repository.update(updatedReservation).as(Some(updatedReservation))
        }

      case None => IO.pure(None)
    }
  }

  def confirmReservation(id: UUID): IO[Option[Reservation]] = {
    repository.find(id).flatMap {
      case Some(reservation) if !reservation.isConfirmed =>
        val halfPrice = reservation.totalPrice / 2
        for {
          _                   <- walletService.checkBalance(reservation.accountId, halfPrice)
          _                   <- walletService.deductFromBalance(reservation.accountId, halfPrice)
          confirmedReservation = reservation.copy(isConfirmed = true)
          _                   <- repository.update(confirmedReservation)
        } yield Some(confirmedReservation)

      case Some(reservation) =>
        IO.raiseError(new Exception("Reservation is already confirmed."))
      case None              => IO.pure(None)
    }
  }

  def getReservation(id: UUID): IO[Option[Reservation]] = {
    repository.find(id)
  }

  def getAllReservations: IO[List[Reservation]] = {
    repository.findAll()
  }

  def getReservationsByAccount(accountId: UUID): IO[List[Reservation]] = {
    repository.findByAccount(accountId)
  }

  def deleteReservation(id: UUID): IO[Unit] = {
    repository.delete(id)
  }
}
