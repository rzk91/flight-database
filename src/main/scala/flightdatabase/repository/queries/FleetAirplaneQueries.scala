package flightdatabase.repository.queries

import doobie.Fragment
import doobie.Query0
import doobie.Update0
import doobie.implicits._
import flightdatabase.domain.fleet_airplane.FleetAirplaneModel

private[repository] object FleetAirplaneQueries {

  def selectAllFleetAirplanes: Query0[FleetAirplaneModel] = selectAll.query[FleetAirplaneModel]

  def insertFleetAirplane(model: FleetAirplaneModel): Update0 =
    sql"""INSERT INTO fleet_airplane
         |  	(fleet_id, airplane_id)
	     |	VALUES (
         |  	${model.fleetId},
         |  	${model.airplaneId}
	     |	)
         |""".stripMargin.update

  def deleteFleetAirplane(id: Long): Update0 = deleteWhereId[FleetAirplaneModel](id)

  private def selectAll: Fragment =
    fr"""
        | SELECT
        |  fa.id, fa.fleet_id, fa.airplane_id
        | FROM fleet_airplane fa
      """.stripMargin
}
