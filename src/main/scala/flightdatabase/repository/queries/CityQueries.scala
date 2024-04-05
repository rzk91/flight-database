package flightdatabase.repository.queries

import doobie.Query0
import doobie.Update0
import doobie.implicits._
import flightdatabase.domain.city.CityModel

private[repository] object CityQueries {

  def selectAllCities: Query0[CityModel] = selectAllQuery[CityModel]

  def insertCity(model: CityModel): Update0 =
    sql"""INSERT INTO city 
         |       (name, country_id, capital, population, 
         |       latitude, longitude)
         |   VALUES (
         |       ${model.name},
         |       ${model.countryId},
         |       ${model.capital},
         |       ${model.population},
         |       ${model.latitude},
         |       ${model.longitude}
         |   )
         | """.stripMargin.update

  def deleteCity(id: Int): Update0 = deleteWhereId[CityModel](id)
}
