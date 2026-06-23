package flightdatabase.syntax

final class DoubleOps(val x: Double) extends AnyVal {
  def isDefined: Boolean = !x.isNaN && !x.isInfinity
  def isUndefined: Boolean = !isDefined
}

trait ToDoubleOps {
  @inline implicit def enrichDouble(d: Double): DoubleOps = new DoubleOps(d)
}

object double extends ToDoubleOps
