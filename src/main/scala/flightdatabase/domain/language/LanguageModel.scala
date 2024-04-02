package flightdatabase.domain.language

import doobie.Fragment
import doobie.implicits._
import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec
import org.http4s.Uri

@ConfiguredJsonCodec final case class LanguageModel(
  id: Option[Long],
  name: String,
  iso2: String,
  iso3: Option[String],
  originalName: String
) extends ModelBase {

  def uri: Uri = ???

  override def updateId(newId: Long): LanguageModel = copy(id = Some(newId))

  override def sqlInsert: Fragment =
    sql"INSERT INTO language (name, iso2, iso3, original_name) VALUES ($name, $iso2, $iso3, $originalName)"
}
