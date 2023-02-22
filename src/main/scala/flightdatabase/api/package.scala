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

  def runQuery[A](query: ConnectionIO[A]): IO[A] = xa.use(query.transact(_))
}
