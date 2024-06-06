package flightdatabase.utils

import cats.Monad
import cats.data.Kleisli
import cats.data.OptionT
import doobie.ConnectionIO
import doobie.Query0
import doobie.Update0
import org.http4s.Response

import java.nio.file.Path
import scala.util.Try

package object implicits {

  @inline implicit def iterableToRichIterable[A](l: Iterable[A]): RichIterable[A] =
    new RichIterable(l)

  @inline implicit def arrayToRichIterable[A](l: Array[A]): RichIterable[A] = new RichIterable(l)
  @inline implicit def stringToRichIterable(s: String): RichIterable[Char] = new RichIterable(s)
  @inline implicit def enrichString(s: String): RichString = new RichString(s)
  @inline implicit def enrichTry[A](t: Try[A]): RichTry[A] = new RichTry(t)
  @inline implicit def enrichOption[A](o: Option[A]): RichOption[A] = new RichOption(o)
  @inline implicit def enrichDouble(d: Double): RichDouble = new RichDouble(d)
  @inline implicit def enrichPath(path: Path): RichPath = new RichPath(path)

  @inline implicit def enrichConnectionIO[A](stmt: ConnectionIO[A]): RichConnectionIOOps[A] =
    new RichConnectionIOOps(stmt)
  @inline implicit def enrichQuery[A](q: Query0[A]): RichQuery[A] = new RichQuery(q)
  @inline implicit def enrichUpdate(update: Update0): RichUpdate = new RichUpdate(update)

  @inline implicit def enrichKleisliResponse[F[_]: Monad, A](
    self: Kleisli[OptionT[F, *], A, Response[F]]
  ): RichKleisliResponse[F, A] =
    new RichKleisliResponse(self)
}
