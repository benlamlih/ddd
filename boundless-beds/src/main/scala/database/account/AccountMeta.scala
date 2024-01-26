package net.benlamlih
package database.account

import domain.account.Currency
import domain.account.Currency.*

import doobie.{Get, Put}

object AccountMeta {
  implicit val currencyGet: Get[Currency] = Get[String].temap(Currency.fromString)
  implicit val currencyPut: Put[Currency] = Put[String].contramap(Currency.asString)
}
