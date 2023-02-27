package flightdatabase.config

import pureconfig.generic.auto._

object Configuration extends ConfigurationBase {

  lazy val dbConfig: DatabaseConfig =
    source.at("database").loadOrThrow[DatabaseConfig]

  lazy val apiConfig: ApiConfig = source.at("api").loadOrThrow[ApiConfig]
}
