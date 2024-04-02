package flightdatabase.domain.city

import doobie.Fragment
import doobie.implicits._
import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec
import org.http4s.Uri

@ConfiguredJsonCodec final case class CityModel(
  id: Option[Long],
  name: String,
  countryId: String,
  capital: Boolean,
  population: Int,
  latitude: Double,
  longitude: Double
) extends ModelBase {

  def uri: Uri = ???

  override def updateId(newId: Long): CityModel = copy(id = Some(newId))

  override def sqlInsert: Fragment =
    sql"""INSERT INTO city 
         |       (name, country_id, capital, population, 
         |       latitude, longitude)
         |   VALUES (
         |       $name,
         |       ${selectIdStmt("country", Some(countryId), keyField = "iso2")},
         |       $capital,
         |       $population,
         |       $latitude,
         |       $longitude
         |   )
         | """.stripMargin
}
