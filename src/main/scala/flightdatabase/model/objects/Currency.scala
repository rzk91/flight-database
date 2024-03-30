package flightdatabase.model.objects

import doobie._
import doobie.implicits._
import flightdatabase.model._
import io.circe.generic.extras._
import org.http4s.Uri

@ConfiguredJsonCodec final case class Currency(
  id: Option[Long],
  name: String,
  iso: String,
  symbol: Option[String]
) extends FlightDbBase {

  def uri: Uri = ???

  override def sqlInsert: Fragment =
    sql"INSERT INTO currency (name, iso, symbol) VALUES ($name, $iso, $symbol)"
}
