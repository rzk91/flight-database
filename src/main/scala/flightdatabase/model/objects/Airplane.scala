package flightdatabase.model.objects

import doobie._
import doobie.implicits._
import flightdatabase.model.objects.FlightDbBase._
import io.circe.generic.extras._
import org.http4s.Uri

@ConfiguredJsonCodec final case class Airplane(
  id: Option[Long],
  name: String,
  @JsonKey("manufacturer") manufacturerId: String,
  capacity: Int,
  maxRangeInKm: Int
) extends FlightDbBase {

  def uri: Uri = ???

  override def sqlInsert: Fragment =
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
