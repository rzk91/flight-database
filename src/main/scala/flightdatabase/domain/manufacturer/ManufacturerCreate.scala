package flightdatabase.domain.manufacturer

import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class ManufacturerCreate(
  id: Option[Long],
  name: String,
  cityBasedIn: Long
)

object ManufacturerCreate {

  def apply(
    name: String,
    cityBasedIn: Long
  ): ManufacturerCreate =
    new ManufacturerCreate(
      None,
      name,
      cityBasedIn
    )
}
