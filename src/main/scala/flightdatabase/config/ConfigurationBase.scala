package flightdatabase.config

import pureconfig.ConfigSource

abstract class ConfigurationBase(resource: String) {
  case class SetupConfig(createScripts: Boolean, cleanDatabase: Boolean)

  case class Access(username: String, password: String)
  case class DatabaseConfig(
    driver: String,
    url: String,
    access: Access,
    baseline: String,
    threadPoolSize: Int
  )

  private[config] val source: ConfigSource = ConfigSource.resources(resource)
}
