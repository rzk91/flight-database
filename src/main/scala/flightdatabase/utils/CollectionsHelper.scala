package flightdatabase.utils

import com.typesafe.scalalogging.LazyLogging

object CollectionsHelper extends LazyLogging {

  implicit class EitherOps[A, B](private val either: Either[A, B])
      extends AnyVal {

    def toOptionWithDebug: Option[B] = either match {
      case Left(value) =>
        logger.info(s"Encountered an error: $value")
        None
      case Right(value) =>
        logger.debug(s"Successfully obtained $value")
        Option(value)
    }
  }
}
