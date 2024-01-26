package net.benlamlih
package database.account

import io.circe.{Codec, Decoder, Encoder, Json}
import io.circe.HCursor
import io.circe.DecodingFailure
import net.benlamlih.domain.account.Currency

object CurrencyCodec {
  implicit val currencyEncoder: Encoder[Currency] = currency => Json.fromString(Currency.asString(currency))

  implicit val currencyDecoder: Decoder[Currency] = (c: HCursor) =>
    c.value.asString match {
      case Some(value) => Currency.fromString(value).left.map(error => DecodingFailure(error, c.history))
      case None        => Left(DecodingFailure(s"error decoding ${c.value} into currency", c.history))
    }

  implicit val currencyCodec: Codec[Currency] = Codec.from(currencyDecoder, currencyEncoder)
}
