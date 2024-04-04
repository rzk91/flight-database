package flightdatabase.domain.language

import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class LanguageModel(
  id: Option[Long],
  name: String,
  iso2: String,
  iso3: Option[String],
  originalName: String
)
