package flightdatabase.domain.fleet_airplane

import flightdatabase.domain.FlightDbTable.FLEET_AIRPLANE
import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class FleetAirplane(
  id: Long,
  fleetId: Long,
  airplaneId: Long
)

object FleetAirplane {
  implicit val fleetAirplaneTableBase: TableBase[FleetAirplane] = TableBase.instance(FLEET_AIRPLANE)

  def fromCreate(id: Long, model: FleetAirplaneCreate): FleetAirplane =
    FleetAirplane(
      id,
      model.fleetId,
      model.airplaneId
    )

  def fromPatch(id: Long, patch: FleetAirplanePatch, original: FleetAirplane): FleetAirplane =
    FleetAirplane(
      id,
      patch.fleetId.getOrElse(original.fleetId),
      patch.airplaneId.getOrElse(original.airplaneId)
    )
}
