package flightdatabase.utils

import cats.data.{NonEmptyList => Nel}
import flightdatabase.domain.TableBase

case class FieldValues[T: TableBase, A](field: String, values: Nel[A]) {
  def table: String = implicitly[TableBase[T]].asString

  override def toString: String = s"$table.$field = ${values.toList.mkString(", ")}"
}
