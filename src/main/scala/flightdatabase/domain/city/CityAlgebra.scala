package flightdatabase.domain.city

trait CityAlgebra[F[_]] {
  def getCities(country: Option[String]): F[List[CityModel]]
  def getCitiesOnlyNames(country: Option[String]): F[List[String]]
  def getCityById(id: Long): F[Option[CityModel]]
  def createCity(city: CityModel): F[CityModel]
  def updateCity(city: CityModel): F[Option[CityModel]]
  def deleteCity(id: Long): F[Option[CityModel]]
}
