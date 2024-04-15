package flightdatabase.utils

import flightdatabase.domain.TableBase

case class FieldValue[T: TableBase, A](field: String, value: A) {
  def table: String = implicitly[TableBase[T]].asString

  override def toString: String = s"$table.$field = $value"
}
