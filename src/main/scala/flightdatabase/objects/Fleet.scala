package flightdatabase.objects

import io.circe.generic.extras.ConfiguredJsonCodec
import flightdatabase.objects.DbObject._

@ConfiguredJsonCodec final case class Fleet(
  name: String,
  iso2: String,
  iso3: String,
  callSign: String,
  hubAt: String,
  countryId: String
) extends DbObject {

  def sqlInsert: String =
    s"""INSERT INTO fleet 
     |       (name, iso2, iso3, call_sign, hub_airport_id, country_id)
     |   VALUES (
     |       '$name', '$iso2', '$iso3', '$callSign',
     |       ${selectIdStmt("airport", Some(hubAt), keyField = "iata")},
     |       ${selectIdStmt("country", Some(countryId), keyField = "iso2")}
     |   );
     | """.stripMargin
}
