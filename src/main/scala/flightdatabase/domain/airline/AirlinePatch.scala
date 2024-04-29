package flightdatabase.domain.airline

import flightdatabase.domain._
import io.circe.generic.extras._

@ConfiguredJsonCodec final case class AirlinePatch(
  name: Option[String] = None,
  iata: Option[String] = None,
  icao: Option[String] = None,
  callSign: Option[String] = None,
  countryId: Option[Long] = None
)
