package flightdatabase.domain.manufacturer

import flightdatabase.domain.FlightDbTable.MANUFACTURER
import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class Manufacturer(
  id: Long,
  name: String,
  baseCityId: Long
)

object Manufacturer {

  implicit val manufacturerTableBase: TableBase[Manufacturer] =
    TableBase.instance(MANUFACTURER, Set("id", "name", "base_city_id"))

  def fromCreate(id: Long, model: ManufacturerCreate): Manufacturer =
    Manufacturer(
      id,
      model.name,
      model.baseCityId
    )

  def fromPatch(id: Long, patch: ManufacturerPatch, original: Manufacturer): Manufacturer =
    Manufacturer(
      id,
      patch.name.getOrElse(original.name),
      patch.baseCityId.getOrElse(original.baseCityId)
    )
}
