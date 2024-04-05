package flightdatabase.repository

import doobie.{Fragment, Query0, Update0}
import doobie.implicits._
import flightdatabase.domain.TableBase

package object queries {

  // Helper methods for queries
  def selectWhereQuery[ST: TableBase, SV, W](
    selectField: String,
    whereField: String,
    whereValue: W
  ): Query0[SV] = {
    selectFragment[ST](selectField) ++ whereFragment(whereField, whereValue)
  }.query[SV]

  def selectAllQuery[T](implicit table: TableBase[T]): Query0[T] =
    (fr"SELECT * FROM" ++ Fragment.const(table.asString)).query[T]

  def deleteWhereId[T](id: Int)(implicit table: TableBase[T]): Update0 =
    (fr"DELETE FROM" ++ Fragment.const(table.asString) ++ fr"WHERE id = $id").update

  def selectFragment[T](field: String)(implicit table: TableBase[T]): Fragment =
    fr"SELECT" ++ Fragment.const(field) ++ fr"FROM" ++ Fragment.const(table.asString)

  def whereFragment[A](
    field: String,
    value: A
  ): Fragment = fr"WHERE" ++ Fragment.const(field) ++ fr"= $value"
}
