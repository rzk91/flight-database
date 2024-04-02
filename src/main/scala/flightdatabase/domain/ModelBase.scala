package flightdatabase.domain

import doobie.Fragment
import doobie.implicits._
import org.http4s.Uri

trait ModelBase {
  def id: Option[Long]
  def uri: Uri

  def updateId(newId: Long): ModelBase
  def sqlInsert: Fragment = fr""

  def selectIdStmt(
    table: String,
    key: Option[String],
    keyField: String = "name"
  ): Fragment =
    key.map { k =>
      fr"(SELECT id FROM" ++ Fragment.const(table) ++ fr"WHERE" ++ Fragment.const(keyField) ++ fr"= $k)"
    }.orNull
}
