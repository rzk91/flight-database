package flightdatabase.objects

import io.circe.generic.extras._
import flightdatabase.objects.DbObject._

@ConfiguredJsonCodec final case class Airplane(
  name: String,
  manufacturerId: String,
  capacity: Int,
  @JsonKey("max_range_in_km") maxRangeInKm: Int
) extends DbObject {

  def sqlInsert: String =
    s"""INSERT INTO airplane 
     |       (name, manufacturer_id, capacity, max_range_in_km)
     |   VALUES (
     |       '$name',
     |       ${selectIdStmt("manufacturer", Some(manufacturerId))},
     |       $capacity, $maxRangeInKm
     |   );
     | """.stripMargin
}
