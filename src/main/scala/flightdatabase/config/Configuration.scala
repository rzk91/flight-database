package flightdatabase.config

import com.typesafe.scalalogging.LazyLogging
import flightdatabase.config.EnvironmentEnum.DEV
import flightdatabase.utils.CollectionsHelper.MoreStringOps
import org.http4s.Uri
import org.http4s.Uri.{Authority, Host, Path, Scheme}
import pureconfig.generic.auto._

import scala.util.Try

object Configuration extends ConfigurationBase with LazyLogging {

  logger.info(s"Effective configuration: ${source.config.map(_.root.render)}")

  lazy val environment: Environment = source.loadOrThrow[Environment]

  lazy val dbConfig: DatabaseConfig =
    source.at("database").loadOrThrow[DatabaseConfig]

  lazy val apiConfig: ApiConfig = source.at("api").loadOrThrow[ApiConfig]

  def cleanDatabase: Boolean = environment.env == DEV && dbConfig.cleanDatabase

  def flightDbBaseUri: Uri =
    Uri(
      Some(Scheme.http),
      Some(
        Authority(
          host = Host.unsafeFromString {
            val original = apiConfig.host
            (for {
              h <- original.toOption
              if h.startsWith("0") || h.startsWith("127")
              host = "localhost"
            } yield host).getOrElse(original)
          },
          port = Try(apiConfig.portNumber).toOption.map(_.value)
        )
      ),
      Path.unsafeFromString("flightdb")
    )
}
