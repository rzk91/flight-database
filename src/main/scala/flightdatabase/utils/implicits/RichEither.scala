package flightdatabase.utils.implicits

class RichEither[A, B](private val either: Either[A, B]) extends AnyVal {

  def foldMap[C, D](fa: A => C, fb: B => D): Either[C, D] =
    either match {
      case Left(value)  => Left(fa(value)).withRight[D]
      case Right(value) => Right(fb(value)).withLeft[C]
    }

  def toOptionWithDebug: Option[B] = either match {
    case Left(value) =>
      println(s"Encountered an error: $value")
      Option.empty[B]
    case Right(value) =>
      println(s"Successfully obtained $value")
      Option(value)
  }
}
