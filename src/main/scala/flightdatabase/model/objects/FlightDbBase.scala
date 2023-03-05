package flightdatabase.model.objects

import doobie._
import doobie.implicits._
import io.circe.generic.extras.Configuration
import org.http4s.Uri

trait FlightDbBase {
  def id: Option[Long]
  def uri: Uri

  def sqlInsert: Fragment = fr""

  def selectIdStmt(
    table: String,
    key: Option[String],
    keyField: String = "name"
  ): Fragment =
    key.map { k =>
      fr"(SELECT id FROM" ++ Fragment.const(table) ++ fr"WHERE" ++ Fragment.const(keyField) ++ fr"= $k)"
    }.orNull // FixMe: This can't be right
}

object FlightDbBase {

  // Allow for snake_case to camelCase conversion automatically
  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames
}
