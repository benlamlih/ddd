package net.benlamlih
package domain.room

sealed trait RoomType
object RoomType {

  case object Standard extends RoomType
  case object Superior extends RoomType
  case object Suite    extends RoomType

  def asString: RoomType => String = {
    case Standard => "Standard"
    case Superior => "Superior"
    case Suite    => "Suite"
  }

  def fromString(s: String): Either[String, RoomType] = s match {
    case "Standard" => Right(Standard)
    case "Superior" => Right(Superior)
    case "Suite"    => Right(Suite)
    case _          => Left(s"Unknown Room Type: $s")
  }
}
