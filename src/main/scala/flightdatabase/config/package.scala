package flightdatabase

import pureconfig._
import pureconfig.error.CannotConvert

package object config {

  implicit val environmentReader: ConfigReader[Environment] = ConfigReader[String].emap { env =>
    env.toUpperCase match {
      case "DEV"  => Right(DEV)
      case "PROD" => Right(PROD)
      case _ =>
        Left(CannotConvert(env, "Environment", "Only available options are: 'DEV' and 'PROD'."))
    }
  }

  private[config] val source: ConfigObjectSource = ConfigSource
    .resources("local.conf")
    .optional
    .withFallback(ConfigSource.resources("application.conf"))
}
