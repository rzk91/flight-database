package flightdatabase.domain.fleet

import flightdatabase.domain._
import io.circe.generic.extras._

@ConfiguredJsonCodec final case class FleetPatch(
  name: Option[String] = None,
  iso2: Option[String] = None,
  iso3: Option[String] = None,
  callSign: Option[String] = None,
  @JsonKey("hub_airport_id") hubAt: Option[Long] = None
)
