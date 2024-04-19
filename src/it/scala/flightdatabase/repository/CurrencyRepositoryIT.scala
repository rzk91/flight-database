package flightdatabase.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.domain.currency.Currency
import flightdatabase.testutils.RepositoryCheck

final class CurrencyRepositoryIT extends RepositoryCheck {

  "Selecting all currencies" should "return the correct detailed list" in {
    val currencies = {
      for {
        repo          <- CurrencyRepository.make[IO]
        allCurrencies <- repo.getCurrencies
      } yield allCurrencies
    }.unsafeRunSync().value.value

    currencies should not be empty
    currencies should contain only (
      Currency(1, "Indian Rupee", "INR", Some("₹")),
      Currency(2, "Euro", "EUR", Some("€")),
      Currency(3, "Swedish Krona", "SEK", Some("kr")),
      Currency(4, "Dirham", "AED", None),
      Currency(5, "US Dollar", "USD", Some("$"))
    )
  }
}
