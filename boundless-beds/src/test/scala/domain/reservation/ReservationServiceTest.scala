package net.benlamlih
package domain.reservation

import domain.room.RoomType
import domain.wallet.WalletService

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.Uuid
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar

import java.time.LocalDate
import java.util.UUID as JavaUUID

class ReservationServiceTest extends AnyFlatSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  var mockReservationRepository: ReservationRepository = _
  var mockWalletService: WalletService                 = _
  var service: ReservationService                      = _
  var uuidStringRes: String                            = _
  var reservationId: Refined[String, Uuid]             = _
  var uuidString: String                               = _
  var accountId: Refined[String, Uuid]                 = _
  var roomType: RoomType                               = _

  override def beforeEach(): Unit = {
    super.beforeEach()

    mockReservationRepository = mock[ReservationRepository]
    mockWalletService = mock[WalletService]
    service = new ReservationService(mockReservationRepository, mockWalletService)
    uuidStringRes = JavaUUID.randomUUID().toString
    reservationId = Refined.unsafeApply[String, Uuid](uuidStringRes)
    uuidString = JavaUUID.randomUUID().toString
    accountId = Refined.unsafeApply[String, Uuid](uuidString)
    roomType = RoomType.Standard
  }

  "calculateTotalPrice" should "calculate the correct price for Standard rooms" in {
    val service    = new ReservationService(mock[ReservationRepository], mock[WalletService])
    val totalPrice = service.calculateTotalPrice(RoomType.Standard, 5)
    totalPrice shouldBe BigDecimal(50 * 5)
  }

  it should "calculate the correct price for Superior rooms" in {
    val service    = new ReservationService(mock[ReservationRepository], mock[WalletService])
    val totalPrice = service.calculateTotalPrice(RoomType.Superior, 3)
    totalPrice shouldBe BigDecimal(100 * 3)
  }

  it should "calculate the correct price for Suite rooms" in {
    val service    = new ReservationService(mock[ReservationRepository], mock[WalletService])
    val totalPrice = service.calculateTotalPrice(RoomType.Suite, 2)
    totalPrice shouldBe BigDecimal(200 * 2)
  }

  "ReservationService" should "create a reservation successfully" in {
    val checkInDate  = LocalDate.now()
    val checkOutDate = checkInDate.plusDays(5)
    val totalPrice   = BigDecimal(250)
    val halfPrice    = totalPrice / 2

    when(mockWalletService.checkBalance(accountId, halfPrice)).thenReturn(IO.unit)
    when(mockWalletService.deductFromBalance(accountId, halfPrice)).thenReturn(IO.unit)
    when(mockReservationRepository.create(any[Reservation])).thenReturn(IO.unit)

    val result = service.createReservation(accountId, roomType, checkInDate, checkOutDate).unsafeRunSync()

    result.accountId shouldBe accountId
    result.roomType shouldBe roomType
    result.totalPrice shouldBe totalPrice
    result.isConfirmed shouldBe false

  }

  "ReservationService" should "fail to create a reservation due to insufficient funds" in {
    val checkInDate  = LocalDate.now()
    val checkOutDate = checkInDate.plusDays(5)
    val totalPrice   = BigDecimal(250)
    val halfPrice    = totalPrice / 2

    when(mockWalletService.checkBalance(accountId, halfPrice))
      .thenReturn(IO.raiseError(new Exception("Insufficient balance")))
    when(mockWalletService.deductFromBalance(accountId, halfPrice)).thenReturn(IO.unit)
    when(mockReservationRepository.create(any[Reservation])).thenReturn(IO.unit)

    assertThrows[Exception] {
      service.createReservation(accountId, roomType, checkInDate, checkOutDate).unsafeRunSync()
    }
  }

  "updateReservation" should "successfully update a reservation" in {
    val checkInDate     = LocalDate.now()
    val newCheckInDate  = LocalDate.now().plusDays(1)
    val newCheckOutDate = newCheckInDate.plusDays(5)

    val existingReservation =
      Reservation(reservationId, accountId, roomType, checkInDate, checkInDate.plusDays(3), BigDecimal(150), false)
    val updatedReservation  = existingReservation.copy(
      checkInDate = newCheckInDate,
      checkOutDate = newCheckOutDate,
      totalPrice = BigDecimal(250)
    )

    when(mockReservationRepository.find(reservationId)).thenReturn(IO.pure(Some(existingReservation)))
    when(mockWalletService.checkBalance(accountId, BigDecimal(100)))
      .thenReturn(IO.unit)
    when(mockWalletService.deductFromBalance(accountId, BigDecimal(100))).thenReturn(IO.unit)
    when(mockReservationRepository.update(updatedReservation)).thenReturn(IO.unit)

    val result = service.updateReservation(reservationId, accountId, newCheckInDate, newCheckOutDate).unsafeRunSync()

    result shouldBe Some(updatedReservation)
  }

  "updateReservation" should "fail to update a reservation due to insufficient funds" in {
    val checkInDate         = LocalDate.now()
    val newCheckInDate      = LocalDate.now().plusDays(1)
    val newCheckOutDate     = newCheckInDate.plusDays(5)
    val existingReservation =
      Reservation(reservationId, accountId, roomType, checkInDate, checkInDate.plusDays(3), BigDecimal(150), false)

    when(mockReservationRepository.find(reservationId)).thenReturn(IO.pure(Some(existingReservation)))
    when(mockWalletService.checkBalance(accountId, BigDecimal(100)))
      .thenReturn(IO.raiseError(new Exception("Insufficient balance")))

    assertThrows[Exception] {
      service.updateReservation(reservationId, accountId, newCheckInDate, newCheckOutDate).unsafeRunSync()
    }
  }

  "updateReservation" should "correctly update the account balance when changing a reservation" in {
    val checkInDate         = LocalDate.now()
    val newCheckInDate      = LocalDate.now().plusDays(1)
    val newCheckOutDate     = newCheckInDate.plusDays(5)
    val existingReservation =
      Reservation(reservationId, accountId, roomType, checkInDate, checkInDate.plusDays(3), BigDecimal(150), false)
    val priceDifference     = BigDecimal(100)

    when(mockReservationRepository.find(reservationId)).thenReturn(IO.pure(Some(existingReservation)))
    when(mockWalletService.checkBalance(accountId, priceDifference)).thenReturn(IO.unit)
    when(mockWalletService.deductFromBalance(accountId, priceDifference)).thenReturn(IO.unit)
    when(mockReservationRepository.update(any[Reservation])).thenReturn(IO.unit)

    val result = service.updateReservation(reservationId, accountId, newCheckInDate, newCheckOutDate).unsafeRunSync()

    verify(mockWalletService).checkBalance(accountId, priceDifference)
    verify(mockWalletService).deductFromBalance(accountId, priceDifference)

    result should not be empty
    result.get.totalPrice shouldBe BigDecimal(250)
  }

  "confirmReservation" should "successfully confirm a reservation" in {
    val reservation = Reservation(
      reservationId,
      accountId,
      RoomType.Standard,
      LocalDate.now(),
      LocalDate.now().plusDays(5),
      BigDecimal(200),
      false
    )
    val halfPrice   = reservation.totalPrice / 2

    when(mockReservationRepository.find(reservationId)).thenReturn(IO.pure(Some(reservation)))
    when(mockWalletService.checkBalance(accountId, halfPrice)).thenReturn(IO.unit)
    when(mockWalletService.deductFromBalance(accountId, halfPrice)).thenReturn(IO.unit)
    when(mockReservationRepository.update(reservation.copy(isConfirmed = true))).thenReturn(IO.unit)

    val result = service.confirmReservation(reservationId).unsafeRunSync()

    result should not be empty
    result.get.isConfirmed shouldBe true
  }

  "confirmReservation" should "fail to confirm an already confirmed reservation" in {
    val reservation = Reservation(
      reservationId,
      accountId,
      RoomType.Standard,
      LocalDate.now(),
      LocalDate.now().plusDays(5),
      BigDecimal(200),
      true
    )

    when(mockReservationRepository.find(reservationId)).thenReturn(IO.pure(Some(reservation)))

    assertThrows[Exception] {
      service.confirmReservation(reservationId).unsafeRunSync()
    }
  }

  "confirmReservation" should "fail to confirm a reservation due to insufficient funds" in {
    val reservation = Reservation(
      reservationId,
      accountId,
      RoomType.Standard,
      LocalDate.now(),
      LocalDate.now().plusDays(5),
      BigDecimal(200),
      false
    )
    val halfPrice   = reservation.totalPrice / 2

    when(mockReservationRepository.find(reservationId)).thenReturn(IO.pure(Some(reservation)))
    when(mockWalletService.checkBalance(accountId, halfPrice))
      .thenReturn(IO.raiseError(new Exception("Insufficient balance")))

    assertThrows[Exception] {
      service.confirmReservation(reservationId).unsafeRunSync()
    }
  }

  "getReservation" should "retrieve a specific reservation" in {
    val reservation = Reservation(
      reservationId,
      accountId,
      RoomType.Standard,
      LocalDate.now(),
      LocalDate.now().plusDays(5),
      BigDecimal(200),
      false
    )

    when(mockReservationRepository.find(reservationId)).thenReturn(IO.pure(Some(reservation)))

    val result = service.getReservation(reservationId).unsafeRunSync()

    result should not be empty
    result.get shouldBe reservation
  }

  "getAllReservations" should "retrieve all reservations" in {
    val reservations = List(
      Reservation(
        Refined.unsafeApply(JavaUUID.randomUUID().toString),
        accountId,
        RoomType.Standard,
        LocalDate.now(),
        LocalDate.now().plusDays(3),
        BigDecimal(150),
        false
      ),
      Reservation(
        Refined.unsafeApply(JavaUUID.randomUUID().toString),
        accountId,
        RoomType.Suite,
        LocalDate.now(),
        LocalDate.now().plusDays(2),
        BigDecimal(400),
        true
      )
    )

    when(mockReservationRepository.findAll()).thenReturn(IO.pure(reservations))

    val result = service.getAllReservations.unsafeRunSync()

    result should have size 2
    result shouldEqual reservations
  }

  "getReservationsByAccount" should "retrieve all reservations for a specific account" in {
    val reservations = List(
      Reservation(
        Refined.unsafeApply(JavaUUID.randomUUID().toString),
        accountId,
        RoomType.Standard,
        LocalDate.now(),
        LocalDate.now().plusDays(3),
        BigDecimal(150),
        false
      )
    )

    when(mockReservationRepository.findByAccount(accountId)).thenReturn(IO.pure(reservations))

    val result = service.getReservationsByAccount(accountId).unsafeRunSync()

    result should not be empty
    result.head.accountId shouldBe accountId
  }

  "deleteReservation" should "successfully delete a reservation" in {
    when(mockReservationRepository.delete(reservationId)).thenReturn(IO.unit)

    noException should be thrownBy service.deleteReservation(reservationId).unsafeRunSync()

    verify(mockReservationRepository).delete(reservationId)
  }

}
