package flightdatabase.domain

import flightdatabase.domain.FlightDbTable.Table

trait TableBase[A] {
  def table: Table
  def fieldTypeMap: Map[String, FieldType]

  def asString: String = table.toString
  def fields: Set[String] = fieldTypeMap.keySet
}

object TableBase {
  @inline final def apply[A](implicit ev: TableBase[A]): TableBase[A] = ev

  @inline def instance[A](t: Table, map: Map[String, FieldType]): TableBase[A] = new TableBase[A] {
    override val table: Table = t
    override val fieldTypeMap: Map[String, FieldType] = map
  }
}
