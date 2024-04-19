package flightdatabase.domain.currency

import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class CurrencyPatch(
  name: Option[String],
  iso: Option[String],
  symbol: Option[String]
)
