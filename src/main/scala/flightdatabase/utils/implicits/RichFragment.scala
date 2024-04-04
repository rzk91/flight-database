package flightdatabase.utils.implicits

import doobie.Fragment
import doobie.implicits._

class RichFragment (private val fragment: Fragment) extends AnyVal {
  def wrap: Fragment = fr"(" ++ fragment ++ fr")"
}
