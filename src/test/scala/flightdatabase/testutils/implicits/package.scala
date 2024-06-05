package flightdatabase.testutils

import cats.effect.IO
import org.http4s.Response

package object implicits {
  @inline implicit def enrichAny[A](a: A): RichAny[A] = new RichAny(a)

  @inline implicit def enrichAnyCollection[C[_], A](ca: C[A]): RichAnyCollection[C, A] =
    new RichAnyCollection(ca)

  @inline implicit def enrichResponseIO(response: Response[IO]): RichResponseIO =
    new RichResponseIO(response)
}
