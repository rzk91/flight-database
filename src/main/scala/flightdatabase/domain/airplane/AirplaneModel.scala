package flightdatabase.domain.airplane

import io.circe.generic.extras._

@ConfiguredJsonCodec final case class AirplaneModel(
  id: Option[Long],
  name: String,
  @JsonKey("manufacturer") manufacturerId: String,
  capacity: Int,
  maxRangeInKm: Int
)
