package flightdatabase.domain.currency

import flightdatabase.domain.FlightDbTable.CURRENCY
import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class Currency(
  id: Long,
  name: String,
  iso: String,
  symbol: Option[String]
)

object Currency {
  implicit val currencyTableBase: TableBase[Currency] = TableBase.instance(CURRENCY)

  def fromCreate(model: CurrencyCreate): Option[Currency] =
    model.id.map { id =>
      Currency(
        id,
        model.name,
        model.iso,
        model.symbol
      )
    }

  def fromCreateUnsafe(model: CurrencyCreate): Currency =
    fromCreate(model).get

  def fromPatch(id: Long, patch: CurrencyPatch, currency: Currency): Currency =
    Currency(
      id,
      patch.name.getOrElse(currency.name),
      patch.iso.getOrElse(currency.iso),
      patch.symbol.orElse(currency.symbol)
    )
}