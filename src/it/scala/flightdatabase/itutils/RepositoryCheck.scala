package flightdatabase.itutils

import cats.effect.Async
import cats.effect.IO
import doobie.util.transactor.Transactor
import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers

trait RepositoryCheck extends PostgreSqlContainerSpec[IO] with Matchers with EitherValues {
  implicit override val async: Async[IO] = IO.asyncForIO
  implicit lazy val xa: Transactor[IO] = transactor
}
