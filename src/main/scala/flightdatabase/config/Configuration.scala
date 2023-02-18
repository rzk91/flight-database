package flightdatabase.config

import pureconfig._
import pureconfig.generic.auto._

object Configuration {
  final case class SetupConfig(createScripts: Boolean, cleanDatabase: Boolean)

  final case class DatabaseConfig(url: String, username: String, password: String, baseline: String)

  lazy val setupConfig: SetupConfig = ConfigSource.default.at("setup").loadOrThrow[SetupConfig]

  lazy val dbConfig: DatabaseConfig =
    ConfigSource.default.at("database").loadOrThrow[DatabaseConfig]
}
