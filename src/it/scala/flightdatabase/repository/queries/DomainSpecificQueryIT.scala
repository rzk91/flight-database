package flightdatabase.repository.queries

import flightdatabase.domain.airplane.AirplaneCreate
import flightdatabase.domain.city.CityModel
import flightdatabase.domain.manufacturer.ManufacturerModel
import flightdatabase.testutils.DbChecker

// TODO: Checks are incomplete (e.g. insert, update, failure checks, etc. are missing)
final class DomainSpecificQueryIT extends DbChecker {

  // Airplane checks
  "All airplane queries" should "work correctly" in {
    check(AirplaneQueries.airplaneExists(1))
    check(AirplaneQueries.selectAllAirplanes)
    check(AirplaneQueries.selectAirplanesBy("id", 1L))
    check(AirplaneQueries.selectAllAirplanesByExternal[ManufacturerModel, String]("name", "Airbus"))
    check(AirplaneQueries.insertAirplane(AirplaneCreate(None, "Boeing 747", 2, 416, 13400)))
    check(AirplaneQueries.deleteAirplane(1))
  }

  // Airport checks
  "All airport queries" should "work correctly" in {
    check(AirportQueries.selectAllAirports)
    check(AirportQueries.selectAllAirportsByExternal[CityModel, String]("name", "Bangalore"))
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
