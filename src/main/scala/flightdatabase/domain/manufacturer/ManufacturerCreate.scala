package flightdatabase.domain.manufacturer

import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class ManufacturerCreate(
  id: Option[Long],
  name: String,
  baseCityId: Long
)

object ManufacturerCreate {

  def apply(
    name: String,
    baseCityId: Long
  ): ManufacturerCreate =
    new ManufacturerCreate(
      None,
      name,
      baseCityId
    )
}
