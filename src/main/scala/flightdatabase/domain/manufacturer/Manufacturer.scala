package flightdatabase.domain.manufacturer

import flightdatabase.domain.FlightDbTable.MANUFACTURER
import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec
import io.circe.generic.extras.JsonKey

@ConfiguredJsonCodec final case class Manufacturer(
  id: Long,
  name: String,
  @JsonKey("city_based_in") basedIn: Long
)

object Manufacturer {
  implicit val manufacturerTableBase: TableBase[Manufacturer] = TableBase.instance(MANUFACTURER)

  def fromCreate(id: Long, model: ManufacturerCreate): Manufacturer =
    Manufacturer(
      id,
      model.name,
      model.basedIn
    )

  def fromPatch(id: Long, patch: ManufacturerPatch, original: Manufacturer): Manufacturer =
    Manufacturer(
      id,
      patch.name.getOrElse(original.name),
      patch.basedIn.getOrElse(original.basedIn)
    )
}
