package flightdatabase.domain

import flightdatabase.domain.FlightDbTable.Table

trait TableBase[A] {
  def table: Table
  val asString: String = table.toString
}

object TableBase {
  @inline final def apply[A](implicit ev: TableBase[A]): TableBase[A] = ev

  @inline def instance[A](t: Table): TableBase[A] = new TableBase[A] {
    override val table: Table = t
  }
}
