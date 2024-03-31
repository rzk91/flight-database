package flightdatabase.model.objects

import doobie._
import doobie.implicits._
import flightdatabase.model._
import io.circe.generic.extras._
import org.http4s.Uri

@ConfiguredJsonCodec final case class City(
  id: Option[Long],
  name: String,
  countryId: String,
  capital: Boolean,
  population: Int,
  latitude: Double,
  longitude: Double
) extends FlightDbBase {

  def uri: Uri = ???

  override def updateId(newId: Long): City = copy(id = Some(newId))

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
