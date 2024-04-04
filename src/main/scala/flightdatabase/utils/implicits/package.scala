package flightdatabase.utils

import doobie.{ConnectionIO, Fragment, Query0, Update0}

import java.nio.file.Path
import scala.util.Try

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

  @inline implicit def enrichConnectionIO[A](stmt: ConnectionIO[A]): RichConnectionIOOps[A] =
    new RichConnectionIOOps(stmt)
  @inline implicit def enrichFragment(f: Fragment): RichFragment = new RichFragment(f)
  @inline implicit def enrichQuery[A](q: Query0[A]): RichQuery[A] = new RichQuery(q)
  @inline implicit def enrichUpdate(update: Update0): RichUpdate = new RichUpdate(update)
}
