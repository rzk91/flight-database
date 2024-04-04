package flightdatabase.repository

import doobie.{Fragment, Query0}
import doobie.implicits._
import flightdatabase.domain.FlightDbTable.Table

package object queries {

  // Helper methods for queries
  def selectWhereQuery[S, W](
    selectField: String,
    selectTable: Table,
    whereField: String,
    whereValue: W
  ): Query0[S] = {
    selectFragment(selectField, selectTable) ++
    whereFragment(whereField, whereValue)
  }.query[S]

  def selectIdWhereNameQuery(
    selectTable: Table,
    whereField: String
  ): Query0[Int] = selectWhereQuery[Int, String]("id", selectTable, "name", whereField)

  def selectFragment(field: String, table: Table): Fragment =
    fr"SELECT" ++ Fragment.const(field) ++ fr"FROM" ++ Fragment.const(table.toString)

  def whereFragment[A](
    field: String,
    value: A
  ): Fragment = fr"WHERE" ++ Fragment.const(field) ++ fr"= $value"
}
