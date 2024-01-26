package net.benlamlih
package http

import database.reservation.RoomTypeCodec.*
import domain.account.UUID
import domain.reservation.{Reservation, ReservationService}
import domain.room.RoomType

import cats.effect.*
import eu.timepit.refined.*
import eu.timepit.refined.api.{RefType, Refined}
import eu.timepit.refined.string.Uuid
import io.circe.*
import io.circe.generic.auto.*
import io.circe.refined.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.io.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.time.LocalDate

class ReservationController(reservationService: ReservationService) {

  implicit val reservationDecoder: EntityDecoder[IO, ReservationRequest]             = jsonOf[IO, ReservationRequest]
  implicit val updateReservationDecoder: EntityDecoder[IO, UpdateReservationRequest] =
    jsonOf[IO, UpdateReservationRequest]

  implicit def logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  private def responseFromReservationOption(reservationOpt: Option[Reservation]): IO[Response[IO]] =
    reservationOpt.fold[IO[Response[IO]]](NotFound("Reservation not found."))(r => Ok(r.asJson))

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {

    case req @ POST -> Root / "reservations" =>
      val io: IO[Response[IO]] =
        for {
          _           <- logger.info("Creating a new reservation")
          request     <- req.decodeJson[ReservationRequest]
          reservation <- reservationService.createReservation(
                           request.accountId,
                           request.roomType,
                           request.checkInDate,
                           request.checkOutDate
                         )
          _           <- logger.info(s"Reservation created with ID: ${reservation.id}")
          res         <- Created(reservation.asJson)
        } yield res
      io.handleErrorWith(e => InternalServerError(e.getMessage))

    case GET -> Root / "reservations" =>
      val io: IO[Response[IO]] =
        for {
          _            <- logger.info(s"Fetching all reservations")
          reservations <- reservationService.getAllReservations
          res          <- Ok(reservations.asJson)
        } yield res
      io.handleErrorWith(e => InternalServerError(e.getMessage))

    case GET -> Root / "reservations" / IdVar(uuid) =>
      val io: IO[Response[IO]] =
        for {
          _           <- logger.info(s"Fetching reservation with ID: $uuid")
          reservation <- reservationService.getReservation(uuid)
          res         <- responseFromReservationOption(reservation)
        } yield res
      io.handleErrorWith(e => InternalServerError(e.getMessage))

    case GET -> Root / "reservations" / "account" / IdVar(accountId) =>
      val io: IO[Response[IO]] =
        for {
          _            <- logger.info(s"Fetching reservations for account ID: $accountId")
          reservations <- reservationService.getReservationsByAccount(accountId)
          res          <- Ok(reservations.asJson)
        } yield res
      io.handleErrorWith(e => InternalServerError(e.getMessage))

    case req @ PUT -> Root / "reservations" / IdVar(uuid) =>
      val io: IO[Response[IO]] =
        for {
          _                  <- logger.info(s"Updating reservation with ID: $uuid")
          request            <- req.decodeJson[UpdateReservationRequest]
          updatedReservation <- reservationService.updateReservation(
                                  uuid,
                                  request.accountId,
                                  request.newCheckInDate,
                                  request.newCheckOutDate
                                )
          res                <- responseFromReservationOption(updatedReservation)
        } yield res
      io.handleErrorWith(e => InternalServerError(e.getMessage))

    case req @ PUT -> Root / "reservations" / IdVar(uuid) / "confirm" =>
      val io: IO[Response[IO]] =
        for {
          _                    <- logger.info(s"Confirming reservation with ID: $uuid")
          confirmedReservation <- reservationService.confirmReservation(uuid)
          res                  <- responseFromReservationOption(confirmedReservation)
        } yield res

      io.handleErrorWith {
        case e: Exception if e.getMessage == "Reservation is already confirmed." => Conflict(e.getMessage)
        case e                                                                   => InternalServerError(e.getMessage)
      }

    case DELETE -> Root / "reservations" / IdVar(uuid) =>
      val io: IO[Response[IO]] =
        for {
          _   <- logger.info(s"Deleting reservation with ID: $uuid")
          _   <- reservationService.deleteReservation(uuid)
          res <- NoContent()
        } yield res
      io.handleErrorWith(e => InternalServerError(e.getMessage))
  }
}

case class ReservationRequest(
    accountId: UUID,
    roomType: RoomType,
    checkInDate: LocalDate,
    checkOutDate: LocalDate
)

case class UpdateReservationRequest(
    accountId: UUID,
    newCheckInDate: LocalDate,
    newCheckOutDate: LocalDate
)
