package net.benlamlih
package http

import database.account.CurrencyCodec.*
import domain.account.*
import domain.wallet.WalletService

import cats.effect.*
import doobie.*
import eu.timepit.refined.*
import eu.timepit.refined.api.{RefType, Refined}
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.string.Uuid
import eu.timepit.refined.types.string.NonEmptyString
import io.circe.*
import io.circe.generic.auto.*
import io.circe.refined.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.io.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

class AccountController(accountService: AccountService, walletService: WalletService) {

  implicit val accountDecoder: EntityDecoder[IO, AccountRequest] = jsonOf[IO, AccountRequest]
  implicit val walletDecoder: EntityDecoder[IO, FundRequest]     = jsonOf[IO, FundRequest]
  implicit val walletDecoder2: EntityDecoder[IO, FundRequestId]  = jsonOf[IO, FundRequestId]

  implicit def logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  private def responseFromAccountOption(accountOpt: Option[Account]): IO[Response[IO]] =
    accountOpt.fold[IO[Response[IO]]](NotFound("Account not found."))(a => Ok(a.asJson))

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {

    case req @ POST -> Root / "accounts" =>
      val io: IO[Response[IO]] =
        for {
          _       <- logger.info("Creating a new account")
          request <- req.decodeJson[AccountRequest]
          account <- accountService.createAccount(
                       request.fullName,
                       request.email,
                       request.phoneNumber,
                       request.balance,
                       Currency.EUR
                     )
          _       <- logger.info(s"Account created with UUID: ${account.id}")
          res     <- Created(account.asJson)
        } yield res
      io.handleErrorWith {
        case e: DecodingFailure =>
          e.history match {
            case CursorOp.DownField("email") :: _       => BadRequest(s"Invalid email format.")
            case CursorOp.DownField("phoneNumber") :: _ => BadRequest(s"Invalid phone number format.")
            case _                                      => InternalServerError(e.getMessage)
          }
        case e                  =>
          InternalServerError(e.getMessage)
      }

    case GET -> Root / "accounts" / IdVar(uuid) =>
      val io: IO[Response[IO]] =
        for {
          _       <- logger.info(s"Fetching account with UUID: $uuid")
          account <- accountService.getAccount(uuid)
          res     <- responseFromAccountOption(account)
        } yield res
      io.handleErrorWith(e => InternalServerError(e.getMessage))

    case GET -> Root / "accounts" =>
      val io: IO[Response[IO]] = for {
        _        <- logger.info("Fetching all accounts")
        accounts <- accountService.getAllAccounts
        res      <- Ok(accounts.asJson)
      } yield res

      io.handleErrorWith(e => InternalServerError(e.getMessage))

    case req @ PUT -> Root / "accounts" / IdVar(uuid) =>
      val io: IO[Response[IO]] = for {
        _       <- logger.info(s"Updating account with UUID: $uuid")
        request <- req.decodeJson[AccountRequest]
        account <- accountService.updateAccount(
                     uuid,
                     request.fullName,
                     request.email,
                     request.phoneNumber,
                     request.balance,
                     Currency.EUR
                   )
        res     <- responseFromAccountOption(account)
      } yield res

      io.handleErrorWith(e => InternalServerError(e.getMessage))

    case DELETE -> Root / "accounts" / IdVar(uuid) =>
      val io = for {
        account <- accountService.deleteAccount(uuid)
        _       <- logger.info(s"Account with UUID: $uuid deleted successfully")
        res     <- NoContent()
      } yield res
      io.handleErrorWith(e => InternalServerError(e.getMessage))

    // Can't call this route using Angular 17 ? path param issue? Works fine with curl
    case req @ POST -> Root / "wallets" / "fund" / IdVar(uuid) =>
      val io: IO[Response[IO]] =
        for {
          _              <- logger.info("Funding an account")
          request        <- req.decodeJson[FundRequest]
          updatedAccount <-
            walletService.fundWallet(uuid, request.amount, request.currency)
          _              <- logger.info(s"Account funded with amount: ${updatedAccount.balance}")
          res            <- Ok(updatedAccount.asJson)
        } yield res
      io.handleErrorWith(e => InternalServerError(e.getMessage))

    case req @ POST -> Root / "wallets" =>
      val io: IO[Response[IO]] =
        for {
          _              <- logger.info("Funding an account")
          request        <- req.decodeJson[FundRequestId]
          updatedAccount <-
            walletService.fundWallet(request.accountId, request.amount, request.currency)
          _              <- logger.info(s"Account funded with amount: ${updatedAccount.balance}")
          res            <- Ok(updatedAccount.asJson)
        } yield res
      io.handleErrorWith(e => InternalServerError(e.getMessage))
  }
}

case class AccountRequest(
    fullName: NonEmptyString,
    email: Email,
    phoneNumber: PhoneNumber,
    balance: BigDecimal
)

case class FundRequest(amount: BigDecimal, currency: Currency)
case class FundRequestId(accountId: UUID, amount: BigDecimal, currency: Currency)
