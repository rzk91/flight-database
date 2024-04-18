package flightdatabase.domain.currency

import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class CurrencyCreate(
  id: Option[Long],
  name: String,
  iso: String,
  symbol: Option[String]
)

object CurrencyCreate {

  def apply(
    name: String,
    iso: String,
    symbol: Option[String]
  ): CurrencyCreate =
    new CurrencyCreate(
      None,
      name,
      iso,
      symbol
    )
}
