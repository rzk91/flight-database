package flightdatabase.domain.manufacturer

import flightdatabase.domain.FlightDbTable.MANUFACTURER
import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class Manufacturer(
  id: Long,
  name: String,
  cityBasedIn: Long
)

object Manufacturer {
  implicit val manufacturerTableBase: TableBase[Manufacturer] = TableBase.instance(MANUFACTURER)

  def fromCreate(id: Long, model: ManufacturerCreate): Manufacturer =
    Manufacturer(
      id,
      model.name,
      model.cityBasedIn
    )

  def fromPatch(id: Long, patch: ManufacturerPatch, original: Manufacturer): Manufacturer =
    Manufacturer(
      id,
      patch.name.getOrElse(original.name),
      patch.cityBasedIn.getOrElse(original.cityBasedIn)
    )
}
