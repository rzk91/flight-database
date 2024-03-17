package flightdatabase.config

import cats.effect.Sync
import cats.effect.Resource
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import flightdatabase.utils.CollectionsHelper.MoreStringOps
import org.http4s.Uri
import org.http4s.Uri.{Authority, Host => UriHost, Path, Scheme}
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._
import com.comcast.ip4s._

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
    logger.info(s"conf")
    conf
  }

  case class Config(env: Environment, dbConfig: DatabaseConfig, apiConfig: ApiConfig) {

    lazy val cleanDatabase: Boolean =
      env == DEV && dbConfig.cleanDatabase

    lazy val flightDbBaseUri: Uri =
      Uri(
        Some(Scheme.http),
        Some(
          Authority(
            host = UriHost.unsafeFromString {
              val original = apiConfig.host
              (for {
                h <- original.toOption
                if h.startsWith("0") || h.startsWith("127")
                host = "localhost"
              } yield host).getOrElse(original)
            },
            port = apiConfig.portNumber.map(_.value).toOption
          )
        ),
        Path.unsafeFromString("flightdb")
      )
  }

  case class Access(username: String, password: String)

  case class DatabaseConfig(
    driver: String,
    url: String,
    access: Access,
    baseline: String,
    threadPoolSize: Int,
    cleanDatabase: Boolean
  )

  case class ApiConfig(host: String, private val port: Int) {
    lazy val hostName: Option[Host] = Host.fromString(host)

    lazy val portNumber: Either[Throwable, Port] =
      Port
        .fromInt(port)
        .toRight(
          new IllegalArgumentException(
            s"Port number $port is invalid. Must be an integer between 0 and 65535."
          )
        )
  }
}
