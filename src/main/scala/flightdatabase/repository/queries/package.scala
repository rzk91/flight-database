package flightdatabase.repository

import doobie.Query0
import flightdatabase.domain.FlightDbTable.Table

package object queries {

  // Helper methods for queries
  def stringQuery(table: Table): Query0[String] =
    getNamesFragment(table).query[String]

  def idWhereNameQuery(table: Table, name: String): Query0[Int] =
    getIdWhereNameFragment(table, name).query[Int]

  def nameWhereIdQuery(mainTable: Table, subTable: Table, id: Int): Query0[String] =
    getNameWhereIdFragment(mainTable, s"${subTable}_id", id).query[String]
}
