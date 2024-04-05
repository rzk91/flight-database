package flightdatabase.domain.fleet

import flightdatabase.domain._
import flightdatabase.domain.FlightDbTable.FLEET
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class FleetModel(
  id: Option[Long],
  name: String,
  iso2: String,
  iso3: String,
  callSign: String,
  hubAt: String,
  countryId: String
)

object FleetModel {
  implicit val fleetModelTable: TableBase[FleetModel] = TableBase.instance(FLEET)
}

// sql"""INSERT INTO fleet
//         |       (name, iso2, iso3, call_sign, hub_airport_id, country_id)
//         |   VALUES (
//         |       $name, $iso2, $iso3, $callSign,
//         |       ${selectIdStmt("airport", Some(hubAt), keyField = "iata")},
//         |       ${selectIdStmt("country", Some(countryId), keyField = "iso2")}
//         |   )
//         | """.stripMargin
