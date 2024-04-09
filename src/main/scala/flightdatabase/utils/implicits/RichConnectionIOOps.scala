package flightdatabase.utils.implicits

import cats.effect.Concurrent
import cats.effect.Resource
import doobie.ConnectionIO
import doobie.Transactor
import doobie.hikari.HikariTransactor
import doobie.implicits._

class RichConnectionIOOps[A](private val stmt: ConnectionIO[A]) extends AnyVal {

  def evalExecute[F[_]: Concurrent](implicit xa: Resource[F, HikariTransactor[F]]): F[A] =
    xa.use(stmt.transact(_))

  def execute[F[_]: Concurrent](implicit xa: Transactor[F]): F[A] = stmt.transact(xa)
}
