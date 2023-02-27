package flightdatabase.model.objects

import doobie._
import doobie.implicits._
import io.circe.generic.extras._
import flightdatabase.model.objects.DbObject._

@ConfiguredJsonCodec final case class Currency(
  id: Option[Long],
  name: String,
  iso: String,
  symbol: Option[String]
) extends DbObject {

  def sqlInsert: Fragment =
    sql"INSERT INTO currency (name, iso, symbol) VALUES ($name, $iso, $symbol)"
}
