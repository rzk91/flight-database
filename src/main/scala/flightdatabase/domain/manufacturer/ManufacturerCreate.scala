package flightdatabase.domain.manufacturer

import flightdatabase.domain._
import io.circe.generic.extras._

@ConfiguredJsonCodec final case class ManufacturerCreate(
  id: Option[Long],
  name: String,
  @JsonKey("city_based_in") basedIn: Long
)
