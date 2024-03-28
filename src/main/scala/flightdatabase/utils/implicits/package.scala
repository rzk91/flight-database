package flightdatabase.utils

import scala.util.Try
import java.nio.file.Path

package object implicits {

  @inline implicit def iterableToRichIterable[A](l: Iterable[A]): RichIterable[A] =
    new RichIterable(l)

  @inline implicit def arrayToRichIterable[A](l: Array[A]): RichIterable[A] = new RichIterable(l)
  @inline implicit def stringToRichIterable(s: String): RichIterable[Char] = new RichIterable(s)
  @inline implicit def enrichEither[A, B](e: Either[A, B]): RichEither[A, B] = new RichEither(e)
  @inline implicit def enrichString(s: String): RichString = new RichString(s)
  @inline implicit def enrichTry[A](t: Try[A]): RichTry[A] = new RichTry(t)
  @inline implicit def enrichDouble(d: Double): RichDouble = new RichDouble(d)
  @inline implicit def enrichPath(path: Path): RichPath = new RichPath(path)
}
