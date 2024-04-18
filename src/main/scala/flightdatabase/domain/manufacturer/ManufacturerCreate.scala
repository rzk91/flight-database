package flightdatabase.domain.manufacturer

import flightdatabase.domain._
import io.circe.generic.extras._

@ConfiguredJsonCodec final case class ManufacturerCreate(
  id: Option[Long],
  name: String,
  @JsonKey("city_based_in") basedIn: Long
)

object ManufacturerCreate {

  def apply(
    name: String,
    basedIn: Long
  ): ManufacturerCreate =
    new ManufacturerCreate(
      None,
      name,
      basedIn
    )
}
