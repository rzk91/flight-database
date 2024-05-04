package flightdatabase.utils.implicits

import scala.util.Try

class RichString(private val str: String) extends AnyVal {
  // Safe conversion methods
  def asInt: Option[Int] = Try(str.trim.toInt).toOption
  def asLong: Option[Long] = Try(str.trim.toLong).toOption
  def asDouble: Option[Double] = Try(str.toDouble).toOption
  def asBigDecimal: Option[BigDecimal] = Try(BigDecimal(str.trim)).toOption

  def asBoolean: Option[Boolean] =
    Try(str.trim.toLowerCase).collect {
      case v: String if v == "true" || v == "1"  => true
      case v: String if v == "false" || v == "0" => false
    }.toOption

  // Other helper methods
  def toOption: Option[String] = if (str.isBlank) None else Some(str)
  def hasValue: Boolean = toOption.isDefined
  def trimEdges: String = str.tail.init
  def validRegex: Boolean = Try(str.r).isSuccess
  def removeSubstring(substr: String): String = str.replace(substr, "")
  def removeSeparator(sep: Char): String = str.replaceAll(escape(sep), "")
  def removeQuotes(): String = removeSeparator('"')
  def removeSpaces(): String = removeSeparator(' ')
  def fixDecimalNotation(sep: Char = ','): String = str.trim.replaceAll(escape(sep), ".")
  def deToEnNotation(): String = removeSeparator('.').fixDecimalNotation()
  def encodeSpacesInUrl(): String = str.replaceAll(" ", "%20")
  def encodePlusInUrl(): String = str.replaceAll("\\+", "%2B")
  def elseIfBlank(default: => String): String = if (str.isBlank) default else str

  def elseIfContains(substr: String, default: => String): String =
    if (str.contains(substr)) default else str

  def splitInTwo(by: Char): (String, String) = {
    val split = str.split(by)
    (split.head, split.last)
  }

  private def escape(char: Char): String = {
    val W = "(\\w)".r
    char match {
      case W(_) => char.toString
      case _    => s"\\$char"
    }
  }
}
