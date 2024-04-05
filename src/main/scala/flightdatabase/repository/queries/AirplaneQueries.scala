package flightdatabase.repository.queries

import doobie.{Query0, Update0}
import doobie.implicits._
import flightdatabase.domain.airplane.AirplaneModel

private[repository] object AirplaneQueries {

  def selectAllAirplanes(maybeManufacturer: Option[String]): Query0[AirplaneModel] = {
    val allAirplanes =
      sql"""
           | SELECT a.id, a.name, m.name, a.capacity, a.max_range_in_km
           | FROM airplane a
           | INNER JOIN manufacturer m
           | ON a.manufacturer_id = m.id
         """.stripMargin

    maybeManufacturer
      .fold(allAirplanes)(m => allAirplanes ++ whereFragment("m.name", m))
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

  def deleteAirplane(id: Int): Update0 = deleteWhereId[AirplaneModel](id)
}
