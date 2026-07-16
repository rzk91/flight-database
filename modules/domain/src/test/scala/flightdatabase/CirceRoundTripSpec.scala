package flightdatabase

import flightdatabase.country.Country
import flightdatabase.currency.Currency
import io.circe.syntax._
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class CirceRoundTripSpec extends AnyFlatSpec with Matchers with EitherValues {

  private val germany = Country(
    id = 1L,
    name = "Germany",
    iso2 = "DE",
    iso3 = "DEU",
    countryCode = 49,
    domainName = Some("de"),
    mainLanguageId = 1L,
    secondaryLanguageId = None,
    tertiaryLanguageId = None,
    currencyId = 1L,
    nationality = "German"
  )

  "Country" should "survive a JSON encode/decode round-trip" in {
    germany.asJson.as[Country].value shouldBe germany
  }

  it should "preserve optional fields that are present" in {
    val withSecondary = germany.copy(secondaryLanguageId = Some(2L))
    withSecondary.asJson.as[Country].value shouldBe withSecondary
  }

  it should "encode member names in snake_case" in {
    val json = germany.asJson.noSpaces
    json should include("country_code")
    json should include("main_language_id")
    json should include("secondary_language_id")
    (json should not).include("countryCode")
  }

  "Currency" should "round-trip with and without an optional symbol" in {
    val euro = Currency(1L, "Euro", "EUR", Some("€"))
    val noSymbol = Currency(2L, "Testcoin", "TST", None)
    euro.asJson.as[Currency].value shouldBe euro
    noSymbol.asJson.as[Currency].value shouldBe noSymbol
  }
}
