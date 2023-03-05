package flightdatabase.model.objects

import doobie._
import doobie.implicits._
import flightdatabase.model.objects.FlightDbBase._
import io.circe.generic.extras._
import org.http4s.Uri

@ConfiguredJsonCodec final case class Airport(
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
) extends FlightDbBase {

  def uri: Uri = ???

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
