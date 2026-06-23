package flightdatabase.manufacturer

import flightdatabase.FlightDbTable.MANUFACTURER
import flightdatabase._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class Manufacturer(
  id: Long,
  name: String,
  baseCityId: Long
)

object Manufacturer {

  implicit val manufacturerTableBase: TableBase[Manufacturer] =
    TableBase.instance(
      MANUFACTURER,
      Map("id" -> LongType, "name" -> StringType, "base_city_id" -> LongType)
    )

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
