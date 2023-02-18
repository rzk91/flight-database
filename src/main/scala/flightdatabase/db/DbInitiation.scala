package flightdatabase.db

import flightdatabase.config.Configuration.dbConfig._
import flightdatabase.config.Configuration.setupConfig
import org.flywaydb.core.Flyway

trait DbInitiation {
  def initiateDb(): Unit = {
    val flyway = Flyway.configure()
        .dataSource(url, username, password)
        .baselineVersion(baseline)
        .load()

    if (setupConfig.cleanDatabase) flyway.clean()
    flyway.migrate()
  }
}