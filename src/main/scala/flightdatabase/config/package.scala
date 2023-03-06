package flightdatabase

import flightdatabase.config.EnvironmentEnum.{DEV, Env}
import pureconfig._

import scala.util.Try

package object config {

  implicit val environmentReader: ConfigReader[Env] = ConfigReader[String].map { s =>
    Try(EnvironmentEnum.withName(s.toUpperCase)).getOrElse(DEV)
  }

  private[config] val source: ConfigSource = ConfigSource
    .resources("local.conf")
    .optional
    .withFallback(ConfigSource.resources("application.conf"))
}
