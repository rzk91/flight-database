package flightdatabase.domain.fleet

import flightdatabase.domain.FlightDbTable.FLEET
import flightdatabase.domain._
import io.circe.generic.extras._

@ConfiguredJsonCodec final case class FleetModel(
  id: Option[Long],
  name: String,
  iso2: String,
  iso3: String,
  callSign: String,
  @JsonKey("hub_airport_id") hubAt: Long
)

object FleetModel {
  implicit val fleetModelTable: TableBase[FleetModel] = TableBase.instance(FLEET)
}
