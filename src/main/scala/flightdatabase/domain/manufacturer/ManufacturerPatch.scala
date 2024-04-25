package flightdatabase.domain.manufacturer

import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class ManufacturerPatch(
  name: Option[String] = None,
  cityBasedIn: Option[Long] = None
)
