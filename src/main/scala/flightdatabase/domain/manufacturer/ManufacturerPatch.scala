package flightdatabase.domain.manufacturer

import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec
import io.circe.generic.extras.JsonKey

@ConfiguredJsonCodec final case class ManufacturerPatch(
  name: Option[String],
  @JsonKey("city_based_in") basedIn: Option[Long]
)
