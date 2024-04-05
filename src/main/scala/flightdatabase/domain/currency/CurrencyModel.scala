package flightdatabase.domain.currency

import flightdatabase.domain.FlightDbTable.CURRENCY
import flightdatabase.domain._
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
