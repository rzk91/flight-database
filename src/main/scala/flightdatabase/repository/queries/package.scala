package flightdatabase.repository

import cats.data.{NonEmptyList => Nel}
import doobie._
import doobie.implicits._
import flightdatabase.api.Operator
import flightdatabase.domain.TableBase

package object queries {

  // Helper methods for queries
  def idExistsQuery[T](id: Long)(implicit T: TableBase[T]): Query0[Boolean] = {
    fr"SELECT EXISTS" ++ Fragments.parentheses(
      fr"SELECT 1 FROM" ++ Fragment.const(T.asString) ++ whereFragment2("id", Nel.one(id))
    )
  }.query[Boolean]

  def selectWhereQuery[ST: TableBase, SV: Read, W: Put](
    selectField: String,
    whereField: String,
    whereValue: W
  ): Query0[SV] = {
    selectFragment[ST](selectField) ++ whereFragment(whereField, whereValue)
  }.query[SV]

  def selectWhereQuery2[ST: TableBase, SV: Read, W: Put](
    selectField: String,
    whereField: String,
    whereValues: Nel[W],
    operator: Operator = Operator.Equals
  ): Query0[SV] = {
    selectFragment[ST](selectField) ++ whereFragment2(whereField, whereValues, operator)
  }.query[SV]

  def deleteWhereId[T](id: Long)(implicit table: TableBase[T]): Update0 =
    (fr"DELETE FROM" ++ Fragment.const(table.asString) ++ fr"WHERE id = $id").update

  def selectFragment[T](field: String)(implicit table: TableBase[T]): Fragment =
    fr"SELECT" ++ Fragment.const(field) ++ fr"FROM" ++ Fragment.const(table.asString)

  def selectFragment[T](fields: Nel[String])(implicit table: TableBase[T]): Fragment =
    fr"SELECT" ++ Fragments.comma(fields.map(f => Fragment.const(f))) ++
      fr"FROM" ++ Fragment.const(table.asString)

  def whereFragment[A: Put](field: String, value: A): Fragment =
    fr"WHERE" ++ Fragment.const(field) ++ fr"= $value"

  def whereFragment2[A: Put](
    field: String,
    values: Nel[A],
    operator: Operator = Operator.Equals
  ): Fragment =
    fr"WHERE" ++ (operator match {
      case Operator.Range      => Fragment.const(field) ++ fr"BETWEEN ${values.head} AND ${values.last}"
      case Operator.In         => Fragments.in(Fragment.const(field), values)
      case Operator.NotIn      => Fragments.notIn(Fragment.const(field), values)
      case Operator.StartsWith => Fragment.const(field) ++ fr"ILIKE ${values.map(s => s"$s%").head}"
      case Operator.EndsWith   => Fragment.const(field) ++ fr"ILIKE ${values.map(s => s"%$s").head}"
      case Operator.Contains   => Fragment.const(field) ++ fr"ILIKE ${values.map(s => s"%$s%").head}"
      case Operator.NotContains =>
        Fragment.const(field) ++ fr"NOT ILIKE ${values.map(s => s"%$s%").head}"
      case _ => Fragment.const(field) ++ Fragment.const(operator.inSql) ++ fr"${values.head}"
    })

  def innerJoinWhereFragment[MT: TableBase, ET: TableBase, EV: Put](
    externalField: String,
    externalValue: EV,
    overrideExternalIdField: Option[String] = None
  ): Fragment = {
    val mainTable = implicitly[TableBase[MT]].asString
    val externalTable = implicitly[TableBase[ET]].asString
    val externalIdField = overrideExternalIdField.getOrElse(s"${externalTable}_id")
    fr"INNER JOIN" ++ Fragment.const(externalTable) ++ fr"ON" ++
    Fragment.const(s"$mainTable.$externalIdField") ++
    fr"=" ++ Fragment.const(s"$externalTable.id") ++
    whereFragment(s"$externalTable.$externalField", externalValue)
  }

  def innerJoinWhereFragment2[MT: TableBase, ET: TableBase, EV: Put](
    externalField: String,
    externalValues: Nel[EV],
    operator: Operator,
    overrideExternalIdField: Option[String] = None
  ): Fragment = {
    val mainTable = implicitly[TableBase[MT]].asString
    val externalTable = implicitly[TableBase[ET]].asString
    val externalIdField = overrideExternalIdField.getOrElse(s"${externalTable}_id")
    fr"INNER JOIN" ++ Fragment.const(externalTable) ++ fr"ON" ++
    Fragment.const(s"$mainTable.$externalIdField") ++
    fr"=" ++ Fragment.const(s"$externalTable.id") ++
    whereFragment2(s"$externalTable.$externalField", externalValues, operator)
  }
}
