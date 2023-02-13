package objects

import io.circe.generic.extras.ConfiguredJsonCodec
import DbObject._

@ConfiguredJsonCodec final case class Manufacturer(
  name: String,
  basedIn: String
) extends DbObject {

  def sqlInsert: String =
    s"""INSERT INTO manufacturer (name, based_in)
        | VALUES ($name, ${selectIdStmt("city", Some(basedIn))});
        """.stripMargin
}
