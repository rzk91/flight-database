package flightdatabase

import cats.effect._
import doobie._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import flightdatabase.config.Configuration.dbConfig

package object db {

  implicit lazy val transactor: Resource[IO, HikariTransactor[IO]] =
    DbInitiation.transactor(dbConfig)

  // Get fragments
  def getNamesFragment(table: String): Fragment = fr"SELECT name FROM" ++ Fragment.const(table)
  def getIdsFragment(table: String): Fragment = fr"SELECT id FROM" ++ Fragment.const(table)

  // Where fragments
  def whereNameFragment(name: String): Fragment = fr"WHERE name = $name"
  def whereIdFragment(id: Int): Fragment = fr"WHERE id = $id"
}
