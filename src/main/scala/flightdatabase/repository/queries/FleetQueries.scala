package flightdatabase.repository.queries

import doobie.Fragment
import doobie.Put
import doobie.Query0
import doobie.Update0
import doobie.implicits._
import flightdatabase.domain.airport.Airport
import flightdatabase.domain.fleet.Fleet
import flightdatabase.domain.fleet.FleetCreate

private[repository] object FleetQueries {

  def fleetExists(id: Long): Query0[Boolean] = idExistsQuery[Fleet](id)

  def selectAllFleets: Query0[Fleet] = selectAll.query[Fleet]

  def selectFleetsBy[V: Put](field: String, value: V): Query0[Fleet] =
    (selectAll ++ whereFragment(s"fleet.$field", value)).query[Fleet]

  def selectFleetByAirport[EV: Put](
    externalField: String,
    externalValue: EV
  ): Query0[Fleet] = {
    selectAll ++ innerJoinWhereFragment[Fleet, Airport, EV](
      externalField,
      externalValue,
      Some("hub_airport_id")
    )
  }.query[Fleet]

  def insertFleet(model: FleetCreate): Update0 =
    sql"""INSERT INTO fleet
         |       (name, iso2, iso3, call_sign, hub_airport_id)
         |   VALUES (
         |       ${model.name}, 
         |       ${model.iso2},
         |       ${model.iso3},
         |       ${model.callSign},
         |       ${model.hubAt}
         |   )
         | """.stripMargin.update

  def modifyFleet(model: Fleet): Update0 =
    sql"""
         | UPDATE fleet
         | SET
         |  name = ${model.name},
         |  iso2 = ${model.iso2},
         |  iso3 = ${model.iso3},
         |  call_sign = ${model.callSign},
         |  hub_airport_id = ${model.hubAt}
         | WHERE id = ${model.id}
       """.stripMargin.update

  def deleteFleet(id: Long): Update0 = deleteWhereId[Fleet](id)

  private def selectAll: Fragment =
    fr"""
        | SELECT
        |  fleet.id, fleet.name, fleet.iso2, fleet.iso3, fleet.call_sign, fleet.hub_airport_id
        | FROM fleet
      """.stripMargin
}
