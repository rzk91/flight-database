package flightdatabase.domain

import flightdatabase.domain.FlightDbTable.Table

trait TableBase[A] {
  def table: Table
  def fields: Set[String]
  def asString: String = table.toString
}

object TableBase {
  @inline final def apply[A](implicit ev: TableBase[A]): TableBase[A] = ev

  @inline def instance[A](t: Table, fieldNames: Set[String]): TableBase[A] = new TableBase[A] {
    override val table: Table = t
    override val fields: Set[String] = fieldNames
  }
}
