package flightdatabase.domain.fleet_airplane

import flightdatabase.domain.FlightDbTable.FLEET_AIRPLANE
import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class FleetAirplaneModel(
  id: Option[Long],
  fleetId: String,
  airplaneId: String
)

object FleetAirplaneModel {

  implicit val fleetAirplaneModelTable: TableBase[FleetAirplaneModel] =
    TableBase.instance(FLEET_AIRPLANE)
}
