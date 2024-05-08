package flightdatabase.api

import enumeratum.Enum
import enumeratum.EnumEntry
import enumeratum.EnumEntry.Snakecase
import enumeratum.NoSuchMember

sealed abstract class Operator(override val entryName: String) extends EnumEntry with Snakecase {
  def inSql: String
}

object Operator extends Enum[Operator] {
  val values = findValues

  case object Equals extends Operator("eq") { val inSql = "=" }
  case object NotEquals extends Operator("neq") { val inSql = "<>" }
  case object Is extends Operator("is") { val inSql = "=" }
  case object IsNot extends Operator("is_not") { val inSql = "<>" }
  case object GreaterThan extends Operator("gt") { val inSql = ">" }
  case object GreaterThanOrEqualTo extends Operator("gteq") { val inSql = ">=" }
  case object LessThan extends Operator("lt") { val inSql = "<" }
  case object LessThanOrEqualTo extends Operator("lteq") { val inSql = "<=" }
  case object RegexMatch extends Operator("regex_match") { val inSql = "~" }
  case object Range extends Operator("range") { val inSql = "BETWEEN" }
  case object In extends Operator("in") { val inSql = "IN" }
  case object NotIn extends Operator("not_in") { val inSql = "NOT IN" }
  case object StartsWith extends Operator("starts_with") { val inSql = "ILIKE" }
  case object EndsWith extends Operator("ends_with") { val inSql = "ILIKE" }
  case object Contains extends Operator("contains") { val inSql = "ILIKE" }
  case object NotContains extends Operator("not_contains") { val inSql = "NOT ILIKE" }

  implicit class StringOperatorOps(private val s: String) extends AnyVal {
    def toOperator: Either[NoSuchMember[Operator], Operator] = withNameInsensitiveEither(s)
  }
}
