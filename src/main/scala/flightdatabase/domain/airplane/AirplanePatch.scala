package flightdatabase.domain.airplane

import flightdatabase.domain._
import io.circe.generic.extras._

@ConfiguredJsonCodec final case class AirplanePatch(
  name: Option[String] = None,
  manufacturerId: Option[Long] = None,
  capacity: Option[Int] = None,
  maxRangeInKm: Option[Int] = None
)
