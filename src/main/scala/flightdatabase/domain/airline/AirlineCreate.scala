package flightdatabase.domain.airline

import flightdatabase.domain._
import io.circe.generic.extras._

@ConfiguredJsonCodec final case class AirlineCreate(
  id: Option[Long],
  name: String,
  iata: String,
  icao: String,
  callSign: String,
  countryId: Long
)

object AirlineCreate {

  def apply(
    name: String,
    iata: String,
    icao: String,
    callSign: String,
    countryId: Long
  ): AirlineCreate =
    new AirlineCreate(
      None,
      name,
      iata,
      icao,
      callSign,
      countryId
    )
}
