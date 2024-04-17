package flightdatabase.domain.fleet

import flightdatabase.domain._
import io.circe.generic.extras._

@ConfiguredJsonCodec final case class FleetCreate(
  id: Option[Long],
  name: String,
  iso2: String,
  iso3: String,
  callSign: String,
  @JsonKey("hub_airport_id") hubAt: Long
)
