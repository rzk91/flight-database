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

  def fromCreate(model: FleetAirplaneCreate): Option[FleetAirplane] =
    model.id.map { id =>
      FleetAirplane(
        id,
        model.fleetId,
        model.airplaneId
      )
    }

  def fromCreateUnsafe(model: FleetAirplaneCreate): FleetAirplane =
    fromCreate(model).get

  def fromPatch(id: Long, patch: FleetAirplanePatch, original: FleetAirplane): FleetAirplane =
    FleetAirplane(
      id,
      patch.fleetId.getOrElse(original.fleetId),
      patch.airplaneId.getOrElse(original.airplaneId)
    )
}
