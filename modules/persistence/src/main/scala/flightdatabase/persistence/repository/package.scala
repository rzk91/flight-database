package flightdatabase.persistence

import cats.Applicative
import cats.data.EitherT
import cats.data.{NonEmptyList => Nel}
import flightdatabase._
import flightdatabase.persistence.repository.queries._
import flightdatabase.persistence.syntax.query._
import flightdatabase.persistence.syntax.sortandlimit._
import org.typelevel.doobie._
import org.typelevel.doobie.implicits._

package object repository {

  // Helper methods to access DB
  def getFieldList[S: TableBase, V: Read](
    sortAndLimit: ValidatedSortAndLimit,
    field: String
  ): ConnectionIO[ApiResult[Nel[V]]] =
    (selectFragment[S](field) ++ sortAndLimit.fragment).query[V].asNel(Some(field))

  def getFieldList[ST: TableBase, SV: Read, WT: TableBase, WV: Put](
    selectField: String,
    whereFieldValues: FieldValues[WT, WV],
    operator: Operator,
    maybeIdField: Option[String] = None
  ): ConnectionIO[ApiResult[Nel[SV]]] = {
    val selectTable = implicitly[TableBase[ST]].asString
    val whereTable = whereFieldValues.table
    val idField = maybeIdField.getOrElse(s"${whereTable}_id")
    EitherT(
      selectWhereQuery[WT, Long, WV](
        "id",
        whereFieldValues.field,
        whereFieldValues.values,
        operator
      ).asNel(Some(whereFieldValues.field), Some(whereFieldValues.values))
    ).flatMapF { whereIds =>
      val ids = whereIds.value
      selectWhereQuery[ST, SV, Long](selectField, idField, ids, Operator.In)
        .asNel(Some(selectField), Some(ids))
    }
  }.value

  def featureNotImplemented[F[_]: Applicative, A]: F[ApiResult[A]] =
    FeatureNotImplemented.elevate[F, A]
}
