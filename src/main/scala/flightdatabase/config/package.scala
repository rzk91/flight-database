package flightdatabase

import pureconfig._

package object config {

  private[config] val source: ConfigSource = ConfigSource
    .resources("local.conf")
    .optional
    .withFallback(ConfigSource.resources("application.conf"))
}
