package objects

import io.circe.generic.extras._
import objects.CirceClass._

@ConfiguredJsonCodec final case class Language(
  name: String,
  iso2: String,
  iso3: Option[String],
  originalName: String
) extends CirceClass {

  def sqlInsert: String =
    s"INSERT INTO language (name, iso2, iso3, original_name) " +
    s"VALUES ('$name', '$iso2', ${insertWithNull(iso3)}, '$originalName');"
}
