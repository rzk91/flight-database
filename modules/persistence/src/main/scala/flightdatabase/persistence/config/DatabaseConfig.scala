package flightdatabase.persistence.config

import cats.effect.Sync
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._

final case class Access(username: String, password: String)

final case class DatabaseConfig(
  driver: String,
  private val baseUrl: String,
  dbName: String,
  access: Access,
  threadPoolSize: Int,
  cleanDatabase: Boolean,
  loggingActive: Boolean
) {
  lazy val url: String = s"$baseUrl/$dbName"
}

object DatabaseConfig {
  private val source = ConfigSource.default.at("db-config")

  def load[F[_]: Sync]: F[DatabaseConfig] = source.loadF[F, DatabaseConfig]()
  def loadUnsafe: DatabaseConfig = source.loadOrThrow[DatabaseConfig]
}
