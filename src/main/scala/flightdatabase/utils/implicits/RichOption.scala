package flightdatabase.utils.implicits

final class RichOption[A](private val opt: Option[A]) extends AnyVal {
  def debug: String = opt.fold("N/A")(_.toString)
}
