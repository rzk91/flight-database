package flightdatabase.testutils.implicits

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.http4s.EntityDecoder
import org.http4s.Response

class RichResponseIO(private val response: Response[IO]) extends AnyVal {
  def extract[A](implicit dec: EntityDecoder[IO, A]): A = response.as[A].unsafeRunSync()
  def string: String = response.bodyText.compile.string.unsafeRunSync()
}
