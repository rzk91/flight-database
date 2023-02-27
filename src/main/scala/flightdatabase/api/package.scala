package flightdatabase

import cats.effect._
import doobie._
import doobie.hikari.HikariTransactor
import doobie.implicits._

package object api {
  implicit class MoreConnectionIOOps[A](private val stmt: ConnectionIO[A]) extends AnyVal {

    def runStmt(implicit xa: Resource[IO, HikariTransactor[IO]]): IO[A] = xa.use(stmt.transact(_))
  }
}
