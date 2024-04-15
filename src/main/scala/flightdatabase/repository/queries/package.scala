package flightdatabase.repository

import doobie._
import doobie.implicits._
import flightdatabase.domain.TableBase

package object queries {

  // Helper methods for queries
  def selectWhereQuery[ST: TableBase, SV: Read, W: Put](
    selectField: String,
    whereField: String,
    whereValue: W
  ): Query0[SV] = {
    selectFragment[ST](selectField) ++ whereFragment(whereField, whereValue)
  }.query[SV]

  def deleteWhereId[T](id: Long)(implicit table: TableBase[T]): Update0 =
    (fr"DELETE FROM" ++ Fragment.const(table.asString) ++ fr"WHERE id = $id").update

  def selectFragment[T](field: String)(implicit table: TableBase[T]): Fragment =
    fr"SELECT" ++ Fragment.const(field) ++ fr"FROM" ++ Fragment.const(table.asString)

  def whereFragment[A: Put](
    field: String,
    value: A
  ): Fragment = fr"WHERE" ++ Fragment.const(field) ++ fr"= $value"

  def innerJoinWhereFragment[MT: TableBase, ET: TableBase, EV: Put](
    externalField: String,
    externalValue: EV
  ): Fragment = {
    val mainTable = implicitly[TableBase[MT]].asString
    val externalTable = implicitly[TableBase[ET]].asString
    fr"INNER JOIN" ++ Fragment.const(externalTable) ++ fr"ON" ++
    Fragment.const(s"$mainTable.${externalTable}_id") ++
    fr"=" ++ Fragment.const(s"$externalTable.id") ++
    whereFragment(s"$externalTable.$externalField", externalValue)
  }
}
