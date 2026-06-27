package flightdatabase

import flightdatabase.airline.AirlineAlgebra
import flightdatabase.airline_airplane.AirlineAirplaneAlgebra
import flightdatabase.airline_city.AirlineCityAlgebra
import flightdatabase.airline_route.AirlineRouteAlgebra
import flightdatabase.airplane.AirplaneAlgebra
import flightdatabase.airport.AirportAlgebra
import flightdatabase.city.CityAlgebra
import flightdatabase.country.CountryAlgebra
import flightdatabase.currency.CurrencyAlgebra
import flightdatabase.language.LanguageAlgebra
import flightdatabase.manufacturer.ManufacturerAlgebra

trait Repositories[F[_]] {
  def airplaneRepository: AirplaneAlgebra[F]
  def airlineRepository: AirlineAlgebra[F]
  def airlineAirplaneRepository: AirlineAirplaneAlgebra[F]
  def airlineCityRepository: AirlineCityAlgebra[F]
  def airlineRouteRepository: AirlineRouteAlgebra[F]
  def airportRepository: AirportAlgebra[F]
  def cityRepository: CityAlgebra[F]
  def countryRepository: CountryAlgebra[F]
  def currencyRepository: CurrencyAlgebra[F]
  def languageRepository: LanguageAlgebra[F]
  def manufacturerRepository: ManufacturerAlgebra[F]
}
