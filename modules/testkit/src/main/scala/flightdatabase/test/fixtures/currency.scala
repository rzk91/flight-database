package flightdatabase.test.fixtures

import cats.data.{NonEmptyList => Nel}
import flightdatabase.currency.Currency

trait CurrencyFixtures {
  val currencies: Nel[Currency] = Nel.of(
    Currency(1, "Indian Rupee", "INR", Some("₹")),
    Currency(2, "Euro", "EUR", Some("€")),
    Currency(3, "Swedish Krona", "SEK", Some("kr")),
    Currency(4, "Dirham", "AED", None),
    Currency(5, "US Dollar", "USD", Some("$"))
  )
}

object currency extends CurrencyFixtures
