package flightdatabase.repository.queries

import doobie.Fragment
import doobie.Query0
import doobie.Update0
import doobie.implicits._
import flightdatabase.domain.airport.AirportModel

private[repository] object AirportQueries {

  def selectAllAirports: Query0[AirportModel] = selectAll.query[AirportModel]

  def insertAirport(model: AirportModel): Update0 =
    sql"""INSERT INTO airport
         |       (name, icao, iata, city_id,
         |       number_of_runways, number_of_terminals, capacity,
         |       international, junction)
         |   VALUES (
         |       ${model.name},
         |       ${model.icao},
         |       ${model.iata},
         |       ${model.cityId},
         |       ${model.numRunways},
         |       ${model.numTerminals},
         |       ${model.capacity},
         |       ${model.international},
         |       ${model.junction}
         |   )
         | """.stripMargin.update

  def deleteAirport(id: Long): Update0 = deleteWhereId[AirportModel](id)

  private def selectAll: Fragment =
    fr"""
        |SELECT
        |  id, name, icao, iata, city_id,
        |  number_of_runways, number_of_terminals, capacity,
        |  international, junction
        |FROM airport
      """.stripMargin

}
