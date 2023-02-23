package flightdatabase

import cats.effect._
import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import flightdatabase.db.DbInitiation
import flightdatabase.config.Configuration.dbConfig

package object db {

  lazy val xa = for {
    ec <- ExecutionContexts.fixedThreadPool[IO](dbConfig.threadPoolSize)
    xa <- DbInitiation.transactor(dbConfig, ec)
  } yield xa

  // Get fragments
  def getNamesFragment(table: String): Fragment = fr"SELECT name FROM" ++ Fragment.const(table)
  def getIdsFragment(table: String): Fragment = fr"SELECT id FROM" ++ Fragment.const(table)

  // Where fragments
  def whereNameFragment(name: String): Fragment = fr"WHERE name = $name"
  def whereIdFragment(id: Int): Fragment = fr"WHERE id = $id"
}
