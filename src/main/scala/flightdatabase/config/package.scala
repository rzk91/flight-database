package flightdatabase

import pureconfig._

package object config {
  private[config] val source: ConfigSource = ConfigSource.resources("application.conf")
}
