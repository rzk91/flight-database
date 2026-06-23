package flightdatabase.manufacturer

import flightdatabase._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class ManufacturerPatch(
  name: Option[String] = None,
  baseCityId: Option[Long] = None
)
