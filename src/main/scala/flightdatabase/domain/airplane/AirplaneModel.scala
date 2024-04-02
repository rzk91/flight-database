package flightdatabase.domain.airplane

import doobie.Fragment
import doobie.implicits._
import flightdatabase.domain._
import io.circe.generic.extras._
import org.http4s.Uri

@ConfiguredJsonCodec final case class AirplaneModel(
  id: Option[Long],
  name: String,
  @JsonKey("manufacturer") manufacturerId: String,
  capacity: Int,
  maxRangeInKm: Int
) extends ModelBase {

  def uri: Uri = ???

  override def updateId(newId: Long): AirplaneModel = copy(id = Some(newId))

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
