package flightdatabase.repository.queries

import doobie.Query0
import doobie.Update0
import doobie.implicits._
import flightdatabase.domain.airport.AirportModel

private[repository] object AirportQueries {

  def selectAllAirports: Query0[AirportModel] = selectAllQuery[AirportModel]

  def insertAirport(model: AirportModel): Update0 =
    sql"""INSERT INTO airport
         |       (name, icao, iata, city_id, country_id,
         |       number_of_runways, number_of_terminals, capacity,
         |       international, junction)
         |   VALUES (
         |       ${model.name},
         |       ${model.icao},
         |       ${model.iata},
         |       ${model.cityId},
         |       ${model.countryId},
         |       ${model.numRunways},
         |       ${model.numTerminals},
         |       ${model.capacity},
         |       ${model.international},
         |       ${model.junction}
         |   )
         | """.stripMargin.update

  def deleteAirport(id: Int): Update0 = deleteWhereId[AirportModel](id)
}
