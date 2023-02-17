package flightdatabase.objects

import io.circe.generic.extras.ConfiguredJsonCodec
import flightdatabase.objects.DbObject._

@ConfiguredJsonCodec final case class Manufacturer(
  name: String,
  basedIn: String
) extends DbObject {

  def sqlInsert: String =
    s"""INSERT INTO manufacturer (name, city_based_in)
        | VALUES ('$name', ${selectIdStmt("city", Some(basedIn))});
        """.stripMargin
}
