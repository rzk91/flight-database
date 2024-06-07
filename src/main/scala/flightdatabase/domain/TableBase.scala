package flightdatabase.domain

trait TableBase[A] {
  def table: FlightDbTable
  def fieldTypeMap: Map[String, FieldType]

  def asString: String = table.entryName
  def fields: Set[String] = fieldTypeMap.keySet
}

object TableBase {
  @inline final def apply[A](implicit ev: TableBase[A]): TableBase[A] = ev

  @inline def instance[A](t: FlightDbTable, map: Map[String, FieldType]): TableBase[A] =
    new TableBase[A] {
      override val table: FlightDbTable = t
      override val fieldTypeMap: Map[String, FieldType] = map
    }
}
