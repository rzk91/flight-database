package flightdatabase.domain.airplane

import flightdatabase.domain.FlightDbTable.AIRPLANE
import flightdatabase.domain.TableBase
import io.circe.generic.extras._

@ConfiguredJsonCodec final case class AirplaneModel(
  id: Option[Long],
  name: String,
  manufacturerId: Int,
  capacity: Int,
  maxRangeInKm: Int
)

object AirplaneModel {
  implicit val airplaneModelTable: TableBase[AirplaneModel] = TableBase.instance(AIRPLANE)
}
