package objects

import io.circe.generic.extras.ConfiguredJsonCodec
import CirceClass._

@ConfiguredJsonCodec final case class Manufacturer(
  name: String,
  basedIn: String
) extends CirceClass {

  def sqlInsert: String =
    s"""INSERT INTO manufacturer (name, based_in)
        | VALUES ($name, ${selectIdStmt("city", Some(basedIn))});
        """.stripMargin
}
