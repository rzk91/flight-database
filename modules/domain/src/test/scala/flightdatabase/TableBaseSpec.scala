package flightdatabase

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
import io.circe.Json
import io.circe.syntax._
import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class TableBaseSpec extends AnyFlatSpec with Matchers with OptionValues {

  "TableBase.apply" should "summon the implicit instance in scope" in {
    (TableBase[Country] should be).theSameInstanceAs(Country.countryTableBase)
    (TableBase[Currency] should be).theSameInstanceAs(Currency.currencyTableBase)
  }

  "the Country instance" should "expose the right table and derived name" in {
    TableBase[Country].table shouldBe FlightDbTable.COUNTRY
    TableBase[Country].asString shouldBe "country"
  }

  it should "map columns to their field types" in {
    val tb = TableBase[Country]
    tb.fieldTypeMap("id") shouldBe LongType
    tb.fieldTypeMap("name") shouldBe StringType
    tb.fieldTypeMap("country_code") shouldBe IntType
  }

  "TableBase.instance" should "build a working instance from a table and a field map" in {
    val tb = TableBase.instance[Unit](FlightDbTable.HELLO_WORLD, Map("greeting" -> StringType))
    tb.asString shouldBe "hello"
    tb.table shouldBe FlightDbTable.HELLO_WORLD
    tb.fields shouldBe Set("greeting")
    tb.fieldTypeMap("greeting") shouldBe StringType
  }

  // One fully-populated sample per entity (values are irrelevant — only the encoded key set is
  // used). Optionals are Some so the key still appears; @JsonKey overrides (e.g. route_number,
  // number_of_runways) come through because circe applies them during encoding.
  private val samples: Map[String, (TableBase[_], Json)] = Map(
    "Airline"         -> (TableBase[Airline], Airline(1L, "n", "IA", "ICA", "CS", 1L).asJson),
    "AirlineAirplane" -> (TableBase[AirlineAirplane], AirlineAirplane(1L, 1L, 1L).asJson),
    "AirlineCity"     -> (TableBase[AirlineCity], AirlineCity(1L, 1L, 1L).asJson),
    "AirlineRoute"    -> (TableBase[AirlineRoute], AirlineRoute(1L, 1L, "R1", 1L, 1L).asJson),
    "Airplane"        -> (TableBase[Airplane], Airplane(1L, "n", 1L, 100, 5000, 800).asJson),
    "Airport" -> (
      TableBase[Airport],
      Airport(
        1L,
        "n",
        "ICA",
        "IA",
        1L,
        2,
        1,
        1000L,
        true,
        false,
        BigDecimal(1.0),
        BigDecimal(2.0),
        TaxiDuration(10),
        TaxiDuration(12)
      ).asJson
    ),
    "City" -> (
      TableBase[City],
      City(1L, "n", 1L, true, 1000L, BigDecimal(1.0), BigDecimal(2.0), "UTC").asJson
    ),
    "Country" -> (
      TableBase[Country],
      Country(1L, "Germany", "DE", "DEU", 49, Some("de"), 1L, Some(2L), Some(3L), 1L, "German").asJson
    ),
    "Currency"     -> (TableBase[Currency], Currency(1L, "Euro", "EUR", Some("€")).asJson),
    "Language"     -> (TableBase[Language], Language(1L, "n", "en", Some("eng"), "English").asJson),
    "Manufacturer" -> (TableBase[Manufacturer], Manufacturer(1L, "n", 1L).asJson)
  )

  // Cross-checks each hand-written fieldTypeMap key set against an independent source (the entity's
  // circe member names) so a renamed/added/dropped column is caught without re-typing the map.
  // Guards field *names* only; column *types* (e.g. Long -> LongType) are verified against the real
  // DB schema in #55, with the compiler-guarantee derivation tracked in #56.
  "every entity's fieldTypeMap keys" should "match its circe member names" in {
    samples.foreach {
      case (label, (tableBase, json)) =>
        withClue(s"$label: ") {
          tableBase.fields shouldBe json.asObject.value.keys.toSet
        }
    }
  }

  it should "cover every entity that has a data table" in {
    val dataTables = FlightDbTable.values.toSet - FlightDbTable.HELLO_WORLD - FlightDbTable.DOCS
    samples.values.map(_._1.table).toSet shouldBe dataTables
  }
}
