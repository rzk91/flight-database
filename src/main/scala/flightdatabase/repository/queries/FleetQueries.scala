package flightdatabase.repository.queries

import doobie.Query0
import doobie.Update0
import doobie.implicits._
import flightdatabase.domain.fleet.FleetModel

private[repository] object FleetQueries {
  def selectAllFleets: Query0[FleetModel] = selectAllQuery[FleetModel]

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

  def deleteFleet(id: Int): Update0 = deleteWhereId[FleetModel](id)
}
