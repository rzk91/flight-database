package flightdatabase.config

import cats.effect.Resource
import cats.effect.Sync
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import flightdatabase.api.config.ApiConfig
import flightdatabase.persistence.config.DatabaseConfig
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._

object Configuration extends LazyLogging {

  // TODO: Add functional logging
  def config[F[_]: Sync]: F[Config] =
    for {
      conf <- source.loadF[F, Config]()
      _    <- Sync[F].delay(logger.info(s"$conf"))
    } yield conf

  def configAsResource[F[_]: Sync]: Resource[F, Config] = Resource.eval(config)

  def configUnsafe: Config = {
    val conf = source.loadOrThrow[Config]
    logger.info(s"$conf")
    conf
  }

  case class Config(env: Environment, dbConfig: DatabaseConfig, apiConfig: ApiConfig) {
    lazy val cleanDatabase: Boolean = env == DEV && dbConfig.cleanDatabase
  }
}
