package flightdatabase.domain.airline_airplane

import flightdatabase.domain.FlightDbTable.AIRLINE_AIRPLANE
import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class AirlineAirplane(
  id: Long,
  airlineId: Long,
  airplaneId: Long
)

object AirlineAirplane {

  implicit val airlineAirplaneTableBase: TableBase[AirlineAirplane] =
    TableBase.instance(AIRLINE_AIRPLANE, Set("id", "airline_id", "airplane_id"))

  def fromCreate(id: Long, model: AirlineAirplaneCreate): AirlineAirplane =
    AirlineAirplane(
      id,
      model.airlineId,
      model.airplaneId
    )

  def fromPatch(id: Long, patch: AirlineAirplanePatch, original: AirlineAirplane): AirlineAirplane =
    AirlineAirplane(
      id,
      patch.airlineId.getOrElse(original.airlineId),
      patch.airplaneId.getOrElse(original.airplaneId)
    )
}
