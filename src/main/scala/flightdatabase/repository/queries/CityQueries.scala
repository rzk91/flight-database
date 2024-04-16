package flightdatabase.repository.queries

import doobie.Fragment
import doobie.Put
import doobie.Query0
import doobie.Update0
import doobie.implicits._
import flightdatabase.domain.TableBase
import flightdatabase.domain.city.City
import flightdatabase.domain.city.CityCreate

private[repository] object CityQueries {

  def cityExists(id: Long): Query0[Boolean] = idExistsQuery[City](id)

  def selectAllCities: Query0[City] = selectAll.query[City]

  def selectCitiesBy[V: Put](field: String, value: V): Query0[City] =
    (selectAll ++ whereFragment(s"city.$field", value)).query[City]

  def selectCitiesByExternal[ET: TableBase, EV: Put](
    externalField: String,
    externalValue: EV
  ): Query0[City] = {
    selectAll ++ innerJoinWhereFragment[City, ET, EV](
      externalField,
      externalValue
    )
  }.query[City]

  def insertCity(model: CityCreate): Update0 =
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

  def modifyCity(model: City): Update0 =
    sql"""
         | UPDATE city
         | SET
         |  name = ${model.name},
         |  country_id = ${model.countryId},
         |  capital = ${model.capital},
         |  population = ${model.population},
         |  latitude = ${model.latitude},
         |  longitude = ${model.longitude},
         |  timezone = ${model.timezone}
         | WHERE id = ${model.id}
       """.stripMargin.update

  def deleteCity(id: Long): Update0 = deleteWhereId[City](id)

  private def selectAll: Fragment =
    fr"""
        | SELECT 
        |   city.id, city.name, city.country_id, city.capital,
        |   city.population, city.latitude, city.longitude, city.timezone
        | FROM city
      """.stripMargin
}
