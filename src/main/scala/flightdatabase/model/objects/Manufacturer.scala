package flightdatabase.model.objects

import doobie._
import doobie.implicits._
import flightdatabase.model._
import io.circe.generic.extras._
import org.http4s.Uri

@ConfiguredJsonCodec final case class Manufacturer(
  id: Option[Long],
  name: String,
  basedIn: String
) extends FlightDbBase {

  def uri: Uri = ???

  override def updateId(newId: Long): Manufacturer = copy(id = Some(newId))

  override def sqlInsert: Fragment =
    sql"INSERT INTO manufacturer (name, city_based_in) VALUES ($name, ${selectIdStmt("city", Some(basedIn))})"
}
