package flightdatabase.repository.queries

import doobie.Fragment
import doobie.Query0
import doobie.Update0
import doobie.implicits._
import flightdatabase.domain.airplane.AirplaneModel

private[repository] object AirplaneQueries {

  def selectAllAirplanes: Query0[AirplaneModel] = selectAll.query[AirplaneModel]

  def selectAllAirplanesByManufacturer(manufacturer: String): Query0[AirplaneModel] = {
    val innerJoinManufacturer =
      fr"INNER JOIN manufacturer m ON a.manufacturer_id = m.id"
    (selectAll ++ innerJoinManufacturer ++ whereFragment("m.name", manufacturer))
      .query[AirplaneModel]
  }

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
         | SELECT a.id, a.name, a.manufacturer_id, a.capacity, a.max_range_in_km
         | FROM airplane a
       """.stripMargin
}
