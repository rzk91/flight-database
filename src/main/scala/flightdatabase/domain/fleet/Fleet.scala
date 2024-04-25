package flightdatabase.domain.fleet

import flightdatabase.domain.FlightDbTable.FLEET
import flightdatabase.domain._
import io.circe.generic.extras._

@ConfiguredJsonCodec final case class Fleet(
  id: Long,
  name: String,
  iso2: String,
  iso3: String,
  callSign: String,
  @JsonKey("hub_airport_id") hubAt: Long
)

object Fleet {
  implicit val fleetTableBase: TableBase[Fleet] = TableBase.instance(FLEET)

  def fromCreate(id: Long, model: FleetCreate): Fleet =
    Fleet(
      id,
      model.name,
      model.iso2,
      model.iso3,
      model.callSign,
      model.hubAt
    )

  def fromPatch(id: Long, patch: FleetPatch, original: Fleet): Fleet =
    Fleet(
      id,
      patch.name.getOrElse(original.name),
      patch.iso2.getOrElse(original.iso2),
      patch.iso3.getOrElse(original.iso3),
      patch.callSign.getOrElse(original.callSign),
      patch.hubAt.getOrElse(original.hubAt)
    )
}
