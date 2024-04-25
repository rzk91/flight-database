package flightdatabase.domain.language

import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class LanguageCreate(
  id: Option[Long],
  name: String,
  iso2: String,
  iso3: Option[String],
  originalName: String
)

object LanguageCreate {

  def apply(
    name: String,
    iso2: String,
    iso3: Option[String],
    originalName: String
  ): LanguageCreate =
    new LanguageCreate(
      None,
      name,
      iso2,
      iso3,
      originalName
    )
}
