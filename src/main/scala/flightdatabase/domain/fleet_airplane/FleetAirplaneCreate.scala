package flightdatabase.domain.fleet_airplane

import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class FleetAirplaneCreate(
  id: Option[Long],
  fleetId: Long,
  airplaneId: Long
)

object FleetAirplaneCreate {

  def apply(
    fleetId: Long,
    airplaneId: Long
  ): FleetAirplaneCreate =
    new FleetAirplaneCreate(
      None,
      fleetId,
      airplaneId
    )
}
