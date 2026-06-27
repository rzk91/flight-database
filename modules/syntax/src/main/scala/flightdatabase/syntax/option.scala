package flightdatabase.syntax

final class OptionOps[A](private val opt: Option[A]) extends AnyVal {
  def debug: String = opt.fold("N/A")(_.toString)
}

trait ToOptionOps {
  @inline implicit def toOptionOps[A](opt: Option[A]): OptionOps[A] = new OptionOps(opt)
}

object option extends ToOptionOps
