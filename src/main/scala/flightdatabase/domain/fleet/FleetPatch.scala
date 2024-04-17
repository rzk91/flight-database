package flightdatabase.domain.fleet

import flightdatabase.domain._
import io.circe.generic.extras._

@ConfiguredJsonCodec final case class FleetPatch(
  name: Option[String],
  iso2: Option[String],
  iso3: Option[String],
  callSign: Option[String],
  @JsonKey("hub_airport_id") hubAt: Option[Long]
)
