package flightdatabase.model.objects

import doobie._
import doobie.implicits._
import io.circe.generic.extras.Configuration

trait DbObject {
  def id: Option[Long]

  def sqlInsert: Fragment

  def selectIdStmt(
    table: String,
    key: Option[String],
    keyField: String = "name"
  ): Fragment =
    key.map { k =>
      fr"(SELECT id FROM" ++ Fragment.const(table) ++ fr"WHERE" ++ Fragment.const(keyField) ++ fr"= $k)"
    }.orNull // FixMe: This can't be right
}

object DbObject {

  // Allow for snake_case to camelCase conversion automatically
  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames
}
