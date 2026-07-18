package flightdatabase.test

import cats.data.{NonEmptyList => Nel}
import flightdatabase.airline.Airline
import flightdatabase.airline_airplane.AirlineAirplane
import flightdatabase.airline_city.AirlineCity
import flightdatabase.airline_route.AirlineRoute
import flightdatabase.airplane.Airplane
import flightdatabase.airport.Airport
import flightdatabase.airport.TaxiDuration
import flightdatabase.city.City
import flightdatabase.country.Country
import flightdatabase.currency.Currency
import flightdatabase.language.Language
import flightdatabase.manufacturer.Manufacturer

/** Shared sample catalogue rows, seeded identically to the test database.
  *
  * Single source of truth for `persistence-it`, `api`, and the future E2E
  * suite: each `val` is the non-empty list of rows for one entity, keyed by
  * the entity's plural name (e.g. `fixtures.airports`).
  */
object fixtures {

  val airlines: Nel[Airline] = Nel.of(
    Airline(1, "Lufthansa", "LH", "DLH", "LUFTHANSA", 2),
    Airline(2, "Emirates", "EK", "UAE", "EMIRATES", 4)
  )

  val airlineAirplanes: Nel[AirlineAirplane] = Nel.of(
    AirlineAirplane(1, 1, 2),
    AirlineAirplane(2, 1, 1),
    AirlineAirplane(3, 1, 3),
    AirlineAirplane(4, 2, 1),
    AirlineAirplane(5, 2, 3)
  )

  val airlineCities: Nel[AirlineCity] = Nel.of(
    AirlineCity(1, 1, 2),
    AirlineCity(2, 2, 4)
  )

  val airlineRoutes: Nel[AirlineRoute] = Nel.of(
    AirlineRoute(1, 1, "LH754", 1, 2),
    AirlineRoute(2, 1, "LH755", 2, 1),
    AirlineRoute(3, 5, "EK565", 2, 3),
    AirlineRoute(4, 5, "EK566", 3, 2),
    AirlineRoute(5, 4, "EK47", 3, 1),
    AirlineRoute(6, 4, "EK46", 1, 3)
  )

  val airplanes: Nel[Airplane] = Nel.of(
    Airplane(1, "A380", 1, 853, 14800, 903),
    Airplane(2, "747-8", 2, 410, 14310, 907),
    Airplane(3, "A320neo", 1, 194, 6300, 828),
    Airplane(4, "787-8", 2, 248, 13530, 903)
  )

  val airports: Nel[Airport] = Nel.of(
    Airport(
      1,
      "Frankfurt am Main Airport",
      "EDDF",
      "FRA",
      2,
      4,
      3,
      65000000,
      international = true,
      junction = true,
      latitude = BigDecimal("50.0333"),
      longitude = BigDecimal("8.5706"),
      taxiOutDuration = TaxiDuration(18),
      taxiInDuration = TaxiDuration(8)
    ),
    Airport(
      2,
      "Kempegowda International Airport",
      "VOBL",
      "BLR",
      1,
      2,
      2,
      16800000,
      international = true,
      junction = false,
      latitude = BigDecimal("13.1986"),
      longitude = BigDecimal("77.7066"),
      taxiOutDuration = TaxiDuration(12),
      taxiInDuration = TaxiDuration(6)
    ),
    Airport(
      3,
      "Dubai International Airport",
      "OMDB",
      "DXB",
      4,
      2,
      3,
      92500000,
      international = true,
      junction = true,
      latitude = BigDecimal("25.2532"),
      longitude = BigDecimal("55.3657"),
      taxiOutDuration = TaxiDuration(15),
      taxiInDuration = TaxiDuration(7)
    )
  )

  val cities: Nel[City] = Nel.of(
    City(
      1,
      "Bangalore",
      1,
      capital = false,
      13193000,
      BigDecimal("12.978889"),
      BigDecimal("77.591667"),
      "Asia/Kolkata"
    ),
    City(
      2,
      "Frankfurt am Main",
      2,
      capital = false,
      791000,
      BigDecimal("50.110556"),
      BigDecimal("8.682222"),
      "Europe/Berlin"
    ),
    City(
      3,
      "Berlin",
      2,
      capital = true,
      3571000,
      BigDecimal("52.52"),
      BigDecimal("13.405"),
      "Europe/Berlin"
    ),
    City(
      4,
      "Dubai",
      4,
      capital = false,
      3490000,
      BigDecimal("23.5"),
      BigDecimal("54.5"),
      "Asia/Dubai"
    ),
    City(
      5,
      "Leiden",
      5,
      capital = false,
      125100,
      BigDecimal("52.16"),
      BigDecimal("4.49"),
      "Europe/Amsterdam"
    ),
    City(
      6,
      "Chicago",
      6,
      capital = false,
      8901000,
      BigDecimal("41.85003"),
      BigDecimal("-87.65005"),
      "America/Chicago"
    )
  )

  val countries: Nel[Country] = Nel.of(
    Country(1, "India", "IN", "IND", 91, Some(".in"), 7, Some(1), Some(3), 1, "Indian"),
    Country(2, "Germany", "DE", "DEU", 49, Some(".de"), 2, None, None, 2, "German"),
    Country(3, "Sweden", "SE", "SWE", 46, Some(".se"), 4, None, None, 3, "Swede"),
    Country(
      4,
      "United Arab Emirates",
      "AE",
      "ARE",
      971,
      Some(".ae"),
      5,
      Some(1),
      None,
      4,
      "Emirati"
    ),
    Country(5, "Netherlands", "NL", "NLD", 31, Some(".nl"), 6, None, None, 2, "Dutch"),
    Country(
      6,
      "United States of America",
      "US",
      "USA",
      1,
      Some(".us"),
      1,
      None,
      None,
      5,
      "US citizen"
    )
  )

  val currencies: Nel[Currency] = Nel.of(
    Currency(1, "Indian Rupee", "INR", Some("₹")),
    Currency(2, "Euro", "EUR", Some("€")),
    Currency(3, "Swedish Krona", "SEK", Some("kr")),
    Currency(4, "Dirham", "AED", None),
    Currency(5, "US Dollar", "USD", Some("$"))
  )

  val languages: Nel[Language] = Nel.of(
    Language(1, "English", "EN", Some("ENG"), "English"),
    Language(2, "German", "DE", Some("DEU"), "Deutsch"),
    Language(3, "Tamil", "TA", Some("TAM"), "Tamil"),
    Language(4, "Swedish", "SV", Some("SWE"), "Svenska"),
    Language(5, "Arabic", "AR", Some("ARA"), "Al-Arabiyyah"),
    Language(6, "Dutch", "NL", Some("NLD"), "Nederlands"),
    Language(7, "Hindi", "HI", Some("HIN"), "Hindi")
  )

  val manufacturers: Nel[Manufacturer] = Nel.of(
    Manufacturer(1, "Airbus", 5),
    Manufacturer(2, "Boeing", 6)
  )
}
