package flightdatabase.utils.implicits

class RichDouble(val x: Double) extends AnyVal {
    def isDefined: Boolean = !x.isNaN && !x.isInfinity
    def isUndefined: Boolean = !isDefined
  }
