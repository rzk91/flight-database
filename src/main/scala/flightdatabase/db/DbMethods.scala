package flightdatabase.db

import doobie._
import doobie.implicits._

object DbMethods {
  def getCountryNames: ConnectionIO[List[String]] =
    sql"SELECT name FROM country".query[String].to[List]

  def getCitiesFromCountry(countryName: String): ConnectionIO[List[String]] =
    for {
      countryId <- sql"SELECT id FROM country WHERE name = $countryName".query[Int].unique
      cities    <- sql"SELECT name FROM city WHERE country_id = $countryId".query[String].to[List]
    } yield cities
}
