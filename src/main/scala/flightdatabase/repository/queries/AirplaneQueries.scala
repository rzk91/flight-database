package flightdatabase.repository.queries

import doobie.Fragment
import doobie.Put
import doobie.Query0
import doobie.Update0
import doobie.implicits._
import flightdatabase.domain.TableBase
import flightdatabase.domain.airplane.AirplaneModel

private[repository] object AirplaneQueries {

  def selectAllAirplanes: Query0[AirplaneModel] = selectAll.query[AirplaneModel]

  def selectAirplanesBy[V: Put](field: String, value: V): Query0[AirplaneModel] =
    (selectAll ++ whereFragment(s"airplane.$field", value)).query[AirplaneModel]

  def selectAllAirplanesByExternal[ET: TableBase, EV: Put](
    externalField: String,
    externalValue: EV
  ): Query0[AirplaneModel] = {
    selectAll ++ innerJoinWhereFragment[AirplaneModel, ET, EV](
      externalField,
      externalValue
    )
  }.query[AirplaneModel]

  def insertAirplane(model: AirplaneModel): Update0 =
    sql"""INSERT INTO airplane
         |       (name, manufacturer_id, capacity, max_range_in_km)
         |   VALUES (
         |       ${model.name},
         |       ${model.manufacturerId},
         |       ${model.capacity},
         |       ${model.maxRangeInKm}
         |   )
         | """.stripMargin.update

  def deleteAirplane(id: Long): Update0 = deleteWhereId[AirplaneModel](id)

  private def selectAll: Fragment =
    fr"""
         | SELECT
         |  airplane.id, airplane.name, airplane.manufacturer_id,
         |  airplane.capacity, airplane.max_range_in_km
         | FROM airplane
       """.stripMargin
}
