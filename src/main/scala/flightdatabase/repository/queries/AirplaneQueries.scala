package flightdatabase.repository.queries

import doobie.implicits._
import doobie.util.query.Query0
import flightdatabase.domain.airplane.AirplaneModel

private[repository] object AirplaneQueries {

  def allAirplanes(maybeManufacturer: Option[String]): Query0[AirplaneModel] = {
    val allAirplanes =
      fr"SELECT a.id, a.name, m.name, a.capacity, a.max_range_in_km" ++
        fr"FROM airplane a INNER JOIN manufacturer m on a.manufacturer_id = m.id"

    maybeManufacturer
      .fold(allAirplanes)(m => allAirplanes ++ fr"WHERE m.name = $m")
      .query[AirplaneModel]
  }
}
