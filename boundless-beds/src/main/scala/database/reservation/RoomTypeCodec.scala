package net.benlamlih
package database.reservation

import domain.room.RoomType

import io.circe.*

object RoomTypeCodec {
  implicit val roomTypeEncoder: Encoder[RoomType] = roomType => Json.fromString(roomType.toString)

  implicit val roomTypeDecoder: Decoder[RoomType] = (c: HCursor) =>
    c.value.asString match {
      case Some(value) => RoomType.fromString(value).left.map(error => DecodingFailure(error, c.history))
      case None        => Left(DecodingFailure(s"error decoding ${c.value} into room type", c.history))
    }

  implicit val roomTypeCodec: Codec[RoomType] = Codec.from(roomTypeDecoder, roomTypeEncoder)
}
