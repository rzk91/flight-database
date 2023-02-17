package flightdatabase

import org.flywaydb.core.Flyway

trait DbInitiation {
  def initiateDb(clean: Boolean = false): Unit = {
    val flyway = Flyway.configure()
        .dataSource("jdbc:postgresql:flights", "postgres", "postgres")
        .baselineVersion("2.0")
        .load()

    if (clean) flyway.clean()
    flyway.migrate()
  }
}