package flightdatabase.model.objects

import io.circe.generic.extras._
import flightdatabase.model.objects.DbObject._
import doobie._
import doobie.implicits._

@ConfiguredJsonCodec final case class Language(
  id: Option[Long],
  name: String,
  iso2: String,
  iso3: Option[String],
  originalName: String
) extends DbObject {

  def sqlInsert: String =
    s"INSERT INTO language (name, iso2, iso3, original_name) " +
    s"VALUES ('$name', '$iso2', ${insertWithNull(iso3)}, '$originalName');"

  override def doobieInsert: Fragment =
    sql"""
      INSERT INTO language (name, iso2, iso3, original_name) 
      VALUES ($name, $iso2, $iso3, $originalName)
    """.stripMargin
}
