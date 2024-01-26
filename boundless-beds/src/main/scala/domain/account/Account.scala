package net.benlamlih
package domain.account

import eu.timepit.refined.*
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.*
import eu.timepit.refined.types.string.NonEmptyString

import java.util.UUID

val emailRegex: String       = "^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}$"
val phoneNumberRegex: String = "^(?:(?:\\+|00)33|0)\\s*[1-9](?:[\\s.-]*\\d{2}){4}$"

type EmailConstraint       = MatchesRegex[`emailRegex`.type]
type PhoneNumberConstraint = MatchesRegex[`phoneNumberRegex`.type]

type UUID        = String Refined Uuid
type Email       = String Refined EmailConstraint
type PhoneNumber = String Refined PhoneNumberConstraint
sealed trait Currency

object Currency {
  case object EUR extends Currency
  case object USD extends Currency
  case object GBP extends Currency
  case object JPY extends Currency
  case object CHF extends Currency

  def asString: Currency => String = {
    case EUR => "EUR"
    case USD => "USD"
    case GBP => "GBP"
    case JPY => "JPY"
    case CHF => "CHF"
  }

  def fromString(s: String): Either[String, Currency] = s match {
    case "EUR" => Right(EUR)
    case "USD" => Right(USD)
    case "GBP" => Right(GBP)
    case "JPY" => Right(JPY)
    case "CHF" => Right(CHF)
    case _     => Left(s"Unknown currency: $s")
  }

}

/** Account Entity representing a client's profile.
  */
case class Account(
    id: UUID,
    fullName: NonEmptyString,
    email: Email,
    phoneNumber: PhoneNumber,
    balance: BigDecimal,
    currency: Currency
)

object Account {

  /** Factory method to create a new account.
    */
  def create(
      fullName: NonEmptyString,
      email: Email,
      phoneNumber: PhoneNumber,
      balance: BigDecimal = BigDecimal(0),
      currency: Currency = Currency.EUR
  ): Account =
    Account(
      Refined.unsafeApply(UUID.randomUUID().toString),
      fullName,
      email,
      phoneNumber,
      balance,
      currency
    )

}
