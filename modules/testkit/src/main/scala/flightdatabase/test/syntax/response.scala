package flightdatabase.test.syntax

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.http4s.EntityDecoder
import org.http4s.Response

final class ResponseIOOps(private val response: Response[IO]) extends AnyVal {
  def extract[A](implicit dec: EntityDecoder[IO, A]): A = response.as[A].unsafeRunSync()
  def string: String = response.bodyText.compile.string.unsafeRunSync()
}

trait ToResponseIOOps {

  @inline implicit def toResponseIOOps(response: Response[IO]): ResponseIOOps =
    new ResponseIOOps(response)
}

object response extends ToResponseIOOps
