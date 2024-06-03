package flightdatabase.repository.queries

import cats.data.{NonEmptyList => Nel}
import flightdatabase.api.Operator
import flightdatabase.domain.ResultOrder
import flightdatabase.domain.ValidatedSortAndLimit
import flightdatabase.domain.airplane.AirplaneCreate
import flightdatabase.domain.city.City
import flightdatabase.domain.country.Country
import flightdatabase.domain.manufacturer.Manufacturer
import flightdatabase.itutils.DbChecker

// TODO: Checks are incomplete (e.g. insert, update, failure checks, etc. are missing)
final class DomainSpecificQueryIT extends DbChecker {

  val emptySortAndLimit: ValidatedSortAndLimit = ValidatedSortAndLimit.empty

  val someSortAndLimit: ValidatedSortAndLimit =
    ValidatedSortAndLimit(Some("name"), Some(ResultOrder.Ascending), Some(1), Some(0))

  // Airplane checks
  "All airplane queries" should "work correctly" in {
    check(AirplaneQueries.airplaneExists(1))
    check(AirplaneQueries.selectAllAirplanes)
    check(AirplaneQueries.selectAirplanesBy("id", Nel.one(1L), Operator.Equals))
    check(
      AirplaneQueries
        .selectAirplanesByExternal[Manufacturer, String]("name", Nel.one("Airbus"), Operator.Equals)
    )
    check(AirplaneQueries.insertAirplane(AirplaneCreate(None, "Boeing 747", 2, 416, 13400)))
    check(AirplaneQueries.deleteAirplane(1))
  }

  // Airport checks
  "All airport queries" should "work correctly" in {
    check(AirportQueries.selectAllAirports)
    check(
      AirportQueries
        .selectAllAirportsByExternal[City, String]("name", Nel.one("Bangalore"), Operator.Equals)
    )
    check(AirportQueries.deleteAirport(1))
  }

  // City checks
  "All city queries" should "work correctly" in {
    check(CityQueries.selectAllCities)
    check(
      CityQueries
        .selectCitiesByExternal[Country, String]("name", Nel.one("Germany"), Operator.Equals)
    )
    check(CityQueries.deleteCity(1))
  }

  // Country checks
  "All country queries" should "work correctly" in {
    check(CountryQueries.selectAllCountries)
    check(CountryQueries.deleteCountry(1))
  }

  // Currency checks
  "All currency queries" should "work correctly" in {
    check(CurrencyQueries.selectAllCurrencies)
    check(CurrencyQueries.deleteCurrency(1))
  }

  // Airline checks
  "All airline queries" should "work correctly" in {
    check(AirlineQueries.selectAllAirlines(emptySortAndLimit))
    check(AirlineQueries.selectAllAirlines(someSortAndLimit))
    check(AirlineQueries.deleteAirline(1))
  }

  // AirlineAirplane checks
  "All airline airplane queries" should "work correctly" in {
    check(AirlineAirplaneQueries.selectAllAirlineAirplanes)
    check(AirlineAirplaneQueries.deleteAirlineAirplane(1))
  }

  // AirlineRoute checks
  "All airline route queries" should "work correctly" in {
    check(AirlineRouteQueries.selectAllAirlineRoutes)
    check(AirlineRouteQueries.deleteAirlineRoute(1))
  }

  // Language checks
  "All language queries" should "work correctly" in {
    check(LanguageQueries.selectAllLanguages)
    check(LanguageQueries.deleteLanguage(1))
  }

  // Manufacturer checks
  "All manufacturer queries" should "work correctly" in {
    check(ManufacturerQueries.selectAllManufacturers)
    check(ManufacturerQueries.deleteManufacturer(1))
  }
}
