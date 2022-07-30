package objects

import objects.CirceClass._
import io.circe.generic.extras._

@ConfiguredJsonCodec final case class Currency(
  name: String,
  iso: String,
  symbol: Option[String]
) extends CirceClass {

  def sqlInsert: String =
    s"INSERT INTO currency (name, iso, symbol) " +
    s"VALUES ('$name', '$iso', ${insertWithNull(symbol)});"
}
