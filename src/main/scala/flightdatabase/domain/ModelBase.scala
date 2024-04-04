package flightdatabase.domain

import doobie.Fragment
import doobie.implicits._
import org.http4s.Uri

trait ModelBase {
  def id: Option[Long]
  def uri: Uri
}
