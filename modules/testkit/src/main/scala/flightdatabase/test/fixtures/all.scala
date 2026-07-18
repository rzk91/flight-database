package flightdatabase.test.fixtures

trait AllFixtures
    extends AirlineFixtures
    with AirlineAirplaneFixtures
    with AirlineCityFixtures
    with AirlineRouteFixtures
    with AirplaneFixtures
    with AirportFixtures
    with CityFixtures
    with CountryFixtures
    with CurrencyFixtures
    with LanguageFixtures
    with ManufacturerFixtures

object all extends AllFixtures
