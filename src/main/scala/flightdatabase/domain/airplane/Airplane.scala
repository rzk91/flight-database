package flightdatabase.domain.airplane

import flightdatabase.domain.FlightDbTable.AIRPLANE
import flightdatabase.domain._
import io.circe.generic.extras._

@ConfiguredJsonCodec final case class Airplane(
  id: Long,
  name: String,
  manufacturerId: Long,
  capacity: Int,
  maxRangeInKm: Int
)

object Airplane {

  implicit val airplaneTableBase: TableBase[Airplane] = TableBase.instance(
    AIRPLANE,
    Map(
      "id"              -> LongType,
      "name"            -> StringType,
      "manufacturer_id" -> LongType,
      "capacity"        -> IntType,
      "max_range_in_km" -> IntType
    )
  )

  def fromCreate(id: Long, model: AirplaneCreate): Airplane =
    Airplane(
      id,
      model.name,
      model.manufacturerId,
      model.capacity,
      model.maxRangeInKm
    )

  def fromPatch(id: Long, patch: AirplanePatch, original: Airplane): Airplane =
    Airplane(
      id,
      patch.name.getOrElse(original.name),
      patch.manufacturerId.getOrElse(original.manufacturerId),
      patch.capacity.getOrElse(original.capacity),
      patch.maxRangeInKm.getOrElse(original.maxRangeInKm)
    )

}
