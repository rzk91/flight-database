package flightdatabase.config

import pureconfig.generic.auto._
import flightdatabase.config.EnvironmentEnum.DEV

object Configuration extends ConfigurationBase {

  lazy val environment: Environment = source.loadOrThrow[Environment]

  lazy val dbConfig: DatabaseConfig =
    source.at("database").loadOrThrow[DatabaseConfig]

  lazy val apiConfig: ApiConfig = source.at("api").loadOrThrow[ApiConfig]

  def cleanDatabase: Boolean = environment.env == DEV && dbConfig.cleanDatabase
}
