package flightdatabase.repository.queries

import doobie.Fragment
import doobie.Query0
import doobie.Update0
import doobie.implicits._
import flightdatabase.domain.city.CityModel

private[repository] object CityQueries {

  def selectAllCities: Query0[CityModel] = selectAll.query[CityModel]

  def selectAllCitiesByCountry(country: String): Query0[CityModel] = {
    val innerJoinCountries = fr"INNER JOIN country ON city.country_id = country.id"
    (selectAll ++ innerJoinCountries ++ whereFragment("country.name", country)).query[CityModel]
  }

  def insertCity(model: CityModel): Update0 =
    sql"""INSERT INTO city 
         |       (name, country_id, capital, population, 
         |       latitude, longitude, timezone)
         |   VALUES (
         |       ${model.name},
         |       ${model.countryId},
         |       ${model.capital},
         |       ${model.population},
         |       ${model.latitude},
         |       ${model.longitude},
         |       ${model.timezone}
         |   )
         | """.stripMargin.update

  def deleteCity(id: Long): Update0 = deleteWhereId[CityModel](id)

  private def selectAll: Fragment =
    fr"""
        | SELECT 
        |   city.id, city.name, city.country_id, city.capital,
        |   city.population, city.latitude, city.longitude, city.timezone
        | FROM city
      """.stripMargin
}
