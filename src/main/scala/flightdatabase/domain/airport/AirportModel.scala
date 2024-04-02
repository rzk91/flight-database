package flightdatabase.domain.airport

import doobie.Fragment
import doobie.implicits._
import flightdatabase.domain._
import io.circe.generic.extras._
import org.http4s.Uri

@ConfiguredJsonCodec final case class AirportModel(
  id: Option[Long],
  name: String,
  icao: String,
  iata: String,
  cityId: String,
  countryId: String,
  @JsonKey("number_of_runways") numRunways: Int,
  @JsonKey("number_of_terminals") numTerminals: Int,
  capacity: Int,
  international: Boolean,
  junction: Boolean
) extends ModelBase {

  def uri: Uri = ???

  override def updateId(newId: Long): AirportModel = copy(id = Some(newId))

  override def sqlInsert: Fragment =
    sql"""INSERT INTO airport 
         |       (name, icao, iata, city_id, country_id, 
         |       number_of_runways, number_of_terminals, capacity, 
         |       international, junction)
         |   VALUES (
         |       $name, $icao, $iata,
         |       ${selectIdStmt("city", Some(cityId))},
         |       ${selectIdStmt("country", Some(countryId), keyField = "iso2")},
         |       $numRunways,
         |       $numTerminals,
         |       $capacity,
         |       $international,
         |       $junction
         |   )
         | """.stripMargin
}
