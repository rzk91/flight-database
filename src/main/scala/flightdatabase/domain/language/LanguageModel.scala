package flightdatabase.domain.language

import flightdatabase.domain._
import flightdatabase.domain.FlightDbTable.LANGUAGE
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class LanguageModel(
  id: Option[Long],
  name: String,
  iso2: String,
  iso3: Option[String],
  originalName: String
)

object LanguageModel {
  implicit val languageModelTable: TableBase[LanguageModel] = TableBase.instance(LANGUAGE)
}
