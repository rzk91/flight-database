package flightdatabase.api.syntax

import enumeratum.NoSuchMember
import flightdatabase.Operator
import flightdatabase.ResultOrder

/** String parsing into domain enums — api-only (used by query-param handling). */
final class StringEnumOps(private val str: String) extends AnyVal {

  def toOrder: Either[NoSuchMember[ResultOrder], ResultOrder] =
    ResultOrder.withNameInsensitiveEither(str)

  def toOperator: Either[NoSuchMember[Operator], Operator] =
    Operator.withNameInsensitiveEither(str)
}

trait ToStringEnumOps {
  @inline implicit def toStringEnumOps(str: String): StringEnumOps = new StringEnumOps(str)
}

object string extends ToStringEnumOps
