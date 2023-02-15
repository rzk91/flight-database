package flightdatabase.objects

import flightdatabase.objects.DbObject._
import io.circe.generic.extras._

@ConfiguredJsonCodec final case class Currency(
  name: String,
  iso: String,
  symbol: Option[String]
) extends DbObject {

  def sqlInsert: String =
    s"INSERT INTO currency (name, iso, symbol) " +
    s"VALUES ('$name', '$iso', ${insertWithNull(symbol)});"
}
