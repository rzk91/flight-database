package flightdatabase.persistence.itutils

import cats.data.{NonEmptyList => Nel}
import cats.effect.Async
import cats.effect.IO
import flightdatabase.SqlError
import flightdatabase.ValidatedSortAndLimit
import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers
import org.typelevel.doobie.util.transactor.Transactor

trait RepositoryCheck extends PostgreSqlContainerSpec[IO] with Matchers with EitherValues {
  implicit override val async: Async[IO] = IO.asyncForIO
  implicit lazy val xa: Transactor[IO] = transactor

  // Helpers
  protected val emptySortAndLimit: ValidatedSortAndLimit = ValidatedSortAndLimit.empty

  protected def sqlErrorInvalidSyntax(
    field: Option[String] = None,
    values: Option[Nel[_]] = None
  ): SqlError =
    SqlError("42601", field, values)
}
