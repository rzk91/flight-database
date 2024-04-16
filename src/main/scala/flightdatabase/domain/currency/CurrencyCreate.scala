package flightdatabase.domain.currency

import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class CurrencyCreate(
  id: Option[Long],
  name: String,
  iso: String,
  symbol: Option[String]
)
