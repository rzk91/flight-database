package flightdatabase.repository

import flightdatabase.repository.queries._
import flightdatabase.testutils.DbChecker

// TODO: Checks are incomplete (e.g. insert, update, failure checks, etc. are missing)
final class DomainSpecificTests extends DbChecker {

  // Airplane checks
  "All airplane queries" should "work correctly" in {
    check(AirplaneQueries.selectAllAirplanes)
    check(AirplaneQueries.selectAllAirplanesByManufacturer("Airbus"))
    check(AirplaneQueries.deleteAirplane(1))
  }

  // Airport checks
  "All airport queries" should "work correctly" in {
    check(AirportQueries.selectAllAirports)
    check(AirportQueries.deleteAirport(1))
  }

  // City checks
  "All city queries" should "work correctly" in {
    check(CityQueries.selectAllCities)
    check(CityQueries.selectAllCitiesByCountry("Germany"))
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

  // Fleet checks
  "All fleet queries" should "work correctly" in {
    check(FleetQueries.selectAllFleets)
    check(FleetQueries.deleteFleet(1))
  }

  // FleetAirplane checks
  "All fleet airplane queries" should "work correctly" in {
    check(FleetAirplaneQueries.selectAllFleetAirplanes)
    check(FleetAirplaneQueries.deleteFleetAirplane(1))
  }

  // FleetRoute checks
  "All fleet route queries" should "work correctly" in {
    check(FleetRouteQueries.selectAllFleetRoutes)
    check(FleetRouteQueries.deleteFleetRoute(1))
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
