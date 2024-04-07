package flightdatabase.domain.manufacturer

import flightdatabase.domain.FlightDbTable.MANUFACTURER
import flightdatabase.domain._
import io.circe.generic.extras._

@ConfiguredJsonCodec final case class ManufacturerModel(
  id: Option[Long],
  name: String,
  @JsonKey("city_based_in") basedIn: Long
)

object ManufacturerModel {

  implicit val manufacturerModelTable: TableBase[ManufacturerModel] =
    TableBase.instance(MANUFACTURER)
}
