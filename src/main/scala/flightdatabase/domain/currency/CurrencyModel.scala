package flightdatabase.domain.currency

import flightdatabase.domain._
import flightdatabase.domain.FlightDbTable.CURRENCY
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class CurrencyModel(
  id: Option[Long],
  name: String,
  iso: String,
  symbol: Option[String]
)

object CurrencyModel {
  implicit val currencyModelTable: TableBase[CurrencyModel] = TableBase.instance(CURRENCY)
}
