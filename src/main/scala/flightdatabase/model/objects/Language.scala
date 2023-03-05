package flightdatabase.model.objects

import doobie._
import doobie.implicits._
import flightdatabase.model.objects.FlightDbBase._
import io.circe.generic.extras._
import org.http4s.Uri

@ConfiguredJsonCodec final case class Language(
  id: Option[Long],
  name: String,
  iso2: String,
  iso3: Option[String],
  originalName: String
) extends FlightDbBase {

  def uri: Uri = ???

  override def sqlInsert: Fragment =
    sql"INSERT INTO language (name, iso2, iso3, original_name) VALUES ($name, $iso2, $iso3, $originalName)"
}
