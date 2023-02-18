package flightdatabase.db.objects

import io.circe.generic.extras.ConfiguredJsonCodec
import flightdatabase.db.objects.DbObject._

@ConfiguredJsonCodec final case class City(
  name: String,
  countryId: String,
  capital: Boolean,
  population: Int,
  latitude: Double,
  longitude: Double
) extends DbObject {

  def sqlInsert: String =
    s"""INSERT INTO city 
       |       (name, country_id, capital, population, 
       |       latitude, longitude)
       |   VALUES (
       |       '$name',
       |       ${selectIdStmt("country", Some(countryId), keyField = "iso2")},
       |       $capital,
       |       $population, $latitude, $longitude
       |   );
       | """.stripMargin
}
