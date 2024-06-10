package flightdatabase.itutils

import cats.effect.Async
import cats.effect.IO
import doobie.util.transactor.Transactor
import flightdatabase.domain.SqlError
import flightdatabase.domain.ValidatedSortAndLimit
import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers

trait RepositoryCheck extends PostgreSqlContainerSpec[IO] with Matchers with EitherValues {
  implicit override val async: Async[IO] = IO.asyncForIO
  implicit lazy val xa: Transactor[IO] = transactor

  // Helper values
  protected val emptySortAndLimit: ValidatedSortAndLimit = ValidatedSortAndLimit.empty
  protected val sqlErrorInvalidSyntax: SqlError = SqlError("42601")
}
