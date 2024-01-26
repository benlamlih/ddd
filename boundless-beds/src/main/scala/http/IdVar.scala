package net.benlamlih
package http

import domain.account.UUID

import cats.effect.Ref
import eu.timepit.refined.*
import eu.timepit.refined.string.Uuid

object IdVar {
  def unapply(arg: String): Option[UUID] = {
    refineV[Uuid](arg).toOption
  }

}
