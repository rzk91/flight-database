package flightdatabase.utils

import flightdatabase.domain.TableBase

case class TableValue[T: TableBase, A](value: A) {
  def asString: String = implicitly[TableBase[T]].asString
}
