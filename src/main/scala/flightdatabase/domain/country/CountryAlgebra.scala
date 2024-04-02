package flightdatabase.domain.country

trait CountryAlgebra[F[_]] {
  def getCountries: F[List[CountryModel]]
  def getCountriesOnlyNames: F[List[String]]
  def getCountryById(id: Long): F[Option[CountryModel]]
  def createCountry(country: CountryModel): F[Long]
  def updateCountry(country: CountryModel): F[Option[CountryModel]]
  def deleteCountry(id: Long): F[Option[CountryModel]]
}
