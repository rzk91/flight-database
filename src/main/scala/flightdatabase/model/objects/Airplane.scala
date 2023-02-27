package flightdatabase.model.objects

import doobie._
import doobie.implicits._
import flightdatabase.model.objects.DbObject._
import io.circe.generic.extras._

@ConfiguredJsonCodec final case class Airplane(
  id: Option[Long],
  name: String,
  manufacturerId: String,
  capacity: Int,
  @JsonKey("max_range_in_km") maxRangeInKm: Int
) extends DbObject {

  def sqlInsert: Fragment =
    sql"""INSERT INTO airplane
     |       (name, manufacturer_id, capacity, max_range_in_km)
     |   VALUES (
     |       $name,
     |       ${selectIdStmt("manufacturer", Some(manufacturerId))},
     |       $capacity,
     |       $maxRangeInKm
     |   )
     | """.stripMargin
}
