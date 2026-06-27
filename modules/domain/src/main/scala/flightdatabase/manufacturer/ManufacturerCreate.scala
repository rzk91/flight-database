package flightdatabase.manufacturer

import flightdatabase._
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
