package flightdatabase

import cats.effect._
import doobie._
import doobie.implicits._
import flightdatabase.db._
import io.circe.Encoder
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe._

package object api {
  implicit def encoder[A: Encoder]: EntityEncoder[IO, A] = jsonEncoderOf[IO, A]

  // TODO: Perhaps make transactor implicit and already perform DB initiation before making it available
  def runStmt[A](query: ConnectionIO[A]): IO[A] = xa.use(query.transact(_))
}
