package flightdatabase.repository.queries

import doobie.Fragment
import doobie.Put
import doobie.Query0
import doobie.Update0
import doobie.implicits._
import flightdatabase.domain.TableBase
import flightdatabase.domain.fleet_airplane.FleetAirplane
import flightdatabase.domain.fleet_airplane.FleetAirplaneCreate

private[repository] object FleetAirplaneQueries {

  def fleetAirplaneExists(id: Long): Query0[Boolean] = idExistsQuery[FleetAirplane](id)

  def selectAllFleetAirplanes: Query0[FleetAirplane] = selectAll.query[FleetAirplane]

  def selectFleetAirplanesBy[V: Put](field: String, value: V): Query0[FleetAirplane] =
    (selectAll ++ whereFragment(s"fleet_airplane.$field", value)).query[FleetAirplane]

  def selectFleetAirplaneByExternal[ET: TableBase, EV: Put](
    externalField: String,
    externalValue: EV
  ): Query0[FleetAirplane] = {
    selectAll ++ innerJoinWhereFragment[FleetAirplane, ET, EV](
      externalField,
      externalValue
    )
  }.query[FleetAirplane]

  def insertFleetAirplane(model: FleetAirplaneCreate): Update0 =
    sql"""INSERT INTO fleet_airplane
         |  	(fleet_id, airplane_id)
	     |	VALUES (
         |  	${model.fleetId},
         |  	${model.airplaneId}
	     |	)
         |""".stripMargin.update

  def modifyFleetAirplane(model: FleetAirplane): Update0 =
    sql"""
         | UPDATE fleet_airplane
         | SET
         |  fleet_id = ${model.fleetId},
         |  airplane_id = ${model.airplaneId}
         | WHERE id = ${model.id}
       """.stripMargin.update

  def deleteFleetAirplane(id: Long): Update0 = deleteWhereId[FleetAirplane](id)

  private def selectAll: Fragment =
    fr"""
        | SELECT
        |  fleet_airplane.id,
        |  fleet_airplane.fleet_id,
        |  fleet_airplane.airplane_id
        | FROM fleet_airplane
      """.stripMargin
}
