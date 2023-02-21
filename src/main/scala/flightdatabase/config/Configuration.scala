package flightdatabase.config

import pureconfig._
import pureconfig.generic.auto._

object Configuration extends ConfigurationBase("application.conf") {

  lazy val setupConfig: SetupConfig = source.at("setup").loadOrThrow[SetupConfig]

  lazy val dbConfig: DatabaseConfig =
    source.at("database").loadOrThrow[DatabaseConfig]
}
