package objects

import io.circe.generic.extras._
import CirceClass._

@ConfiguredJsonCodec final case class Airplane(
  name: String,
  manufacturerId: String,
  capacity: Int,
  maxRangeKm: Int
) extends CirceClass {

  def sqlInsert: String =
    s"""INSERT INTO airplane 
     |       (name, manufacturer_id, capacity, maxRangeKm)
     |   VALUES (
     |       '$name',
     |       ${selectIdStmt("manufacturer", Some(manufacturerId))},
     |       $capacity, $maxRangeKm
     |   );
     | """.stripMargin
}
