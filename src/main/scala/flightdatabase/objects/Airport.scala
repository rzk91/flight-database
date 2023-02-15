package flightdatabase.objects

import io.circe.generic.extras._
import flightdatabase.objects.DbObject._

@ConfiguredJsonCodec final case class Airport(
  name: String,
  icao: String,
  iata: String,
  cityId: String,
  countryId: String,
  hubTo: Option[String],
  @JsonKey("number_of_runways") numRunways: Int,
  @JsonKey("number_of_terminals") numTerminals: Int,
  capacity: Int,
  international: Boolean,
  junction: Boolean
) extends DbObject {

  def sqlInsert: String =
    s"""INSERT INTO airport 
     |       (name, icao, iata, city_id, country_id, hub_to, 
     |       number_of_runways, number_of_terminals, capacity, 
     |       international, junction)
     |   VALUES (
     |       '$name', '$icao', '$iata',
     |       ${selectIdStmt("city", Some(cityId))},
     |       ${selectIdStmt("country", Some(countryId), keyField = "iso2")},
     |       ${selectIdStmt("fleet", hubTo)},
     |       $numRunways, $numTerminals, $capacity,
     |       $international, $junction
     |   );
     | """.stripMargin
}
