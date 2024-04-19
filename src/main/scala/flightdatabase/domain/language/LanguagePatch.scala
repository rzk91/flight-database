package flightdatabase.domain.language

import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class LanguagePatch(
  name: Option[String],
  iso2: Option[String],
  iso3: Option[String],
  originalName: Option[String]
)
