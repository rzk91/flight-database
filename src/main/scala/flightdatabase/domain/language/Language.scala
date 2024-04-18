package flightdatabase.domain.language

import flightdatabase.domain.FlightDbTable.LANGUAGE
import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class Language(
  id: Long,
  name: String,
  iso2: String,
  iso3: Option[String],
  originalName: String
)

object Language {
  implicit val languageTableBase: TableBase[Language] = TableBase.instance(LANGUAGE)

  def fromCreate(id: Long, model: LanguageCreate): Language =
    Language(
      id,
      model.name,
      model.iso2,
      model.iso3,
      model.originalName
    )

  def fromPatch(id: Long, patch: LanguagePatch, original: Language): Language =
    Language(
      id,
      patch.name.getOrElse(original.name),
      patch.iso2.getOrElse(original.iso2),
      patch.iso3.orElse(original.iso3),
      patch.originalName.getOrElse(original.originalName)
    )
}
