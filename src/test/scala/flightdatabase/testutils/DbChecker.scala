package flightdatabase.testutils

import cats.effect.IO
import doobie.scalatest.IOChecker
import doobie.Transactor
import flightdatabase.config.Configuration
import flightdatabase.db.Database
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

trait DbChecker extends AnyFlatSpec with Matchers with IOChecker {

  protected val config: Configuration.Config = Configuration.configUnsafe
  protected val db: Database[IO] = Database.makeUnsafe(config.dbConfig, config.cleanDatabase)

  final override val transactor: Transactor[IO] = db.simpleTransactor
}
