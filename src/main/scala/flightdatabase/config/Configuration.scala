package flightdatabase.config

import pureconfig._
import pureconfig.generic.auto._

object Configuration {
  final case class SetupConfig(createScripts: Boolean, cleanDatabase: Boolean)

  final case class Access(username: String, password: String)
  final case class DatabaseConfig(
    driver: String,
    url: String,
    access: Access,
    baseline: String,
    threadPoolSize: Int
  )

  lazy val setupConfig: SetupConfig = ConfigSource.default.at("setup").loadOrThrow[SetupConfig]

  lazy val dbConfig: DatabaseConfig =
    ConfigSource.default.at("database").loadOrThrow[DatabaseConfig]
}
