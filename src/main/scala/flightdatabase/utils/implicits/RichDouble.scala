package flightdatabase.utils.implicits

final class RichDouble(val x: Double) extends AnyVal {
  def isDefined: Boolean = !x.isNaN && !x.isInfinity
  def isUndefined: Boolean = !isDefined
}
