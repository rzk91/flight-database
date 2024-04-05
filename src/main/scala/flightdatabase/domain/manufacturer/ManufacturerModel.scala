package flightdatabase.domain.manufacturer

import flightdatabase.domain._
import flightdatabase.domain.FlightDbTable.MANUFACTURER
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class ManufacturerModel(
  id: Option[Long],
  name: String,
  basedIn: String
)

object ManufacturerModel {

  implicit val manufacturerModelTable: TableBase[ManufacturerModel] =
    TableBase.instance(MANUFACTURER)
}
//    sql"INSERT INTO manufacturer (name, city_based_in) VALUES ($name, ${selectIdStmt("city", Some(basedIn))})"
