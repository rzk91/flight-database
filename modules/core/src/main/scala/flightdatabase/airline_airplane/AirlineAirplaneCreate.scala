package flightdatabase.airline_airplane

import flightdatabase._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class AirlineAirplaneCreate(
  id: Option[Long],
  airlineId: Long,
  airplaneId: Long
)

object AirlineAirplaneCreate {

  def apply(
    airlineId: Long,
    airplaneId: Long
  ): AirlineAirplaneCreate =
    new AirlineAirplaneCreate(
      None,
      airlineId,
      airplaneId
    )
}
