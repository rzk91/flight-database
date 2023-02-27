package flightdatabase

import pureconfig._
import scala.util.Try
import flightdatabase.config.EnvironmentEnum.DEV

package object config {

  implicit val environmentReader = ConfigReader[String].map { s =>
    Try(EnvironmentEnum.withName(s.toUpperCase)).getOrElse(DEV)
  }

  private[config] val source: ConfigSource = ConfigSource
    .resources("local.conf")
    .optional
    .withFallback(ConfigSource.resources("application.conf"))
}
