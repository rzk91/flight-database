package flightdatabase.domain.airplane

import flightdatabase.domain._
import io.circe.generic.extras._

@ConfiguredJsonCodec final case class AirplaneUpdateModel(
  name: Option[String],
  manufacturerId: Option[Long],
  capacity: Option[Int],
  maxRangeInKm: Option[Int]  
)