package flightdatabase

import flightdatabase.country.Country
import flightdatabase.currency.Currency
import io.circe.syntax._
import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class TableBaseSpec extends AnyFlatSpec with Matchers with OptionValues {

  // Fully-populated samples (including optionals) so every column appears as a JSON key.
  private val country =
    Country(1L, "Germany", "DE", "DEU", 49, Some("de"), 1L, Some(2L), Some(3L), 1L, "German")
  private val currency = Currency(1L, "Euro", "EUR", Some("€"))

  "TableBase.apply" should "summon the implicit instance in scope" in {
    (TableBase[Country] should be).theSameInstanceAs(Country.countryTableBase)
    (TableBase[Currency] should be).theSameInstanceAs(Currency.currencyTableBase)
  }

  "the Country instance" should "expose the right table and derived name" in {
    TableBase[Country].table shouldBe FlightDbTable.COUNTRY
    TableBase[Country].asString shouldBe "country"
  }

  it should "map every column to its field type" in {
    val tb = TableBase[Country]
    tb.fields shouldBe Set(
      "id",
      "name",
      "iso2",
      "iso3",
      "country_code",
      "domain_name",
      "main_language_id",
      "secondary_language_id",
      "tertiary_language_id",
      "currency_id",
      "nationality"
    )
    tb.fieldTypeMap("id") shouldBe LongType
    tb.fieldTypeMap("name") shouldBe StringType
    tb.fieldTypeMap("country_code") shouldBe IntType
  }

  "the Currency instance" should "derive its name and fields" in {
    TableBase[Currency].asString shouldBe "currency"
    TableBase[Currency].fields shouldBe Set("id", "name", "iso", "symbol")
    TableBase[Currency].fieldTypeMap("symbol") shouldBe StringType
  }

  // Cross-checks the hand-written fieldTypeMap keys against an independent source (the entity's
  // circe member names) so a renamed/added/dropped column is caught without re-typing the map.
  // This guards field *names* only; column *types* (e.g. Long -> LongType) are verified against
  // the real DB schema in an integration test — see the persistence-it ticket.
  "the fieldTypeMap keys" should "match the entity's JSON member names" in {
    TableBase[Country].fields shouldBe country.asJson.asObject.value.keys.toSet
    TableBase[Currency].fields shouldBe currency.asJson.asObject.value.keys.toSet
  }

  "TableBase.instance" should "build a working instance from a table and a field map" in {
    val tb = TableBase.instance[Unit](FlightDbTable.HELLO_WORLD, Map("greeting" -> StringType))
    tb.asString shouldBe "hello"
    tb.table shouldBe FlightDbTable.HELLO_WORLD
    tb.fields shouldBe Set("greeting")
    tb.fieldTypeMap("greeting") shouldBe StringType
  }
}
