package flightdatabase

import cats.effect._
import doobie.util.ExecutionContexts
import flightdatabase.db.DbInitiation
import flightdatabase.config.Configuration.dbConfig

package object db {
  lazy val xa = for {
    ec <- ExecutionContexts.fixedThreadPool[IO](dbConfig.threadPoolSize)
    xa <- DbInitiation.transactor(dbConfig, ec)
  } yield xa
}
