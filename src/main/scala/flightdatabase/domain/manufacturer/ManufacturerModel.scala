package flightdatabase.domain.manufacturer

import doobie.Fragment
import doobie.implicits._
import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec
import org.http4s.Uri

@ConfiguredJsonCodec final case class ManufacturerModel(
  id: Option[Long],
  name: String,
  basedIn: String
) extends ModelBase {

  def uri: Uri = ???

  override def updateId(newId: Long): ManufacturerModel = copy(id = Some(newId))

  override def sqlInsert: Fragment =
    sql"INSERT INTO manufacturer (name, city_based_in) VALUES ($name, ${selectIdStmt("city", Some(basedIn))})"
}
