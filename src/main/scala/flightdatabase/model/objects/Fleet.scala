package flightdatabase.model.objects

import doobie._
import doobie.implicits._
import flightdatabase.model.objects.FlightDbBase._
import io.circe.generic.extras._

@ConfiguredJsonCodec final case class Fleet(
  id: Option[Long],
  name: String,
  iso2: String,
  iso3: String,
  callSign: String,
  hubAt: String,
  countryId: String
) extends FlightDbBase {

  def sqlInsert: Fragment =
    sql"""INSERT INTO fleet 
     |       (name, iso2, iso3, call_sign, hub_airport_id, country_id)
     |   VALUES (
     |       $name, $iso2, $iso3, $callSign,
     |       ${selectIdStmt("airport", Some(hubAt), keyField = "iata")},
     |       ${selectIdStmt("country", Some(countryId), keyField = "iso2")}
     |   )
     | """.stripMargin
}
