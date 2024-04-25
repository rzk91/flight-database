package flightdatabase.domain.language

import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class LanguagePatch(
  name: Option[String] = None,
  iso2: Option[String] = None,
  iso3: Option[String] = None,
  originalName: Option[String] = None
)
