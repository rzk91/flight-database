package flightdatabase.repository.queries

import doobie.Fragment
import doobie.Query0
import doobie.Update0
import doobie.implicits._
import flightdatabase.domain.fleet.FleetModel

private[repository] object FleetQueries {
  def selectAllFleets: Query0[FleetModel] = selectAll.query[FleetModel]

  def insertFleet(model: FleetModel): Update0 =
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

  def deleteFleet(id: Long): Update0 = deleteWhereId[FleetModel](id)

  private def selectAll: Fragment =
    fr"""
        | SELECT
        |  fleet.id, fleet.name, fleet.iso2, fleet.iso3, fleet.call_sign, fleet.hub_airport_id
        | FROM fleet
      """.stripMargin
}
