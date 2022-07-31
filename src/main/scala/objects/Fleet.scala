package objects

import io.circe.generic.extras.ConfiguredJsonCodec
import CirceClass._

@ConfiguredJsonCodec final case class Fleet(
  name: String,
  iso2: String,
  iso3: String,
  callSign: String,
  hubAt: String,
  countryId: String
) extends CirceClass {

  def sqlInsert: String =
    s"""INSERT INTO fleet 
     |       (name, iso2, iso3, call_sign, hub_at, country_id)
     |   VALUES (
     |       '$name', '$iso2', '$iso3', '$callSign',
     |       ${selectIdStmt("city", Some(hubAt))},
     |       ${selectIdStmt("country", Some(countryId), keyField = "iso2")}
     |   );
     | """.stripMargin
}
