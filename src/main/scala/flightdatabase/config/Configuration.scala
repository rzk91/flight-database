package flightdatabase.config

import cats.effect.Resource
import cats.effect.Sync
import cats.implicits._
import com.comcast.ip4s._
import com.typesafe.scalalogging.LazyLogging
import flightdatabase.utils.CollectionsHelper.MoreStringOps
import org.http4s.Uri
import org.http4s.Uri.Authority
import org.http4s.Uri.Path
import org.http4s.Uri.Scheme
import org.http4s.Uri.{Host => UriHost}
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
    logger.info(s"conf")
    conf
  }

  case class Config(env: Environment, dbConfig: DatabaseConfig, apiConfig: ApiConfig) {

    lazy val cleanDatabase: Boolean = env == DEV && dbConfig.cleanDatabase
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

  case class ApiLogging(active: Boolean, withHeaders: Boolean, withBody: Boolean)

  case class ApiConfig(
    private val host: String,
    private val port: Int,
    entryPoint: String,
    logging: ApiLogging
  ) {
    lazy val hostName: Option[Host] = Host.fromString(host)

    lazy val portNumber: Either[Throwable, Port] =
      Port
        .fromInt(port)
        .toRight(
          new IllegalArgumentException(
            s"Port number $port is invalid. Must be an integer between 0 and 65535."
          )
        )

    lazy val flightDbBaseUri: Uri =
      Uri(
        Some(Scheme.http),
        Some(
          Authority(
            host = UriHost.unsafeFromString {
              val original = host
              (for {
                h <- original.toOption
                if h.startsWith("0") || h.startsWith("127")
                host = "localhost"
              } yield host).getOrElse(original)
            },
            port = portNumber.map(_.value).toOption
          )
        ),
        Path.unsafeFromString(entryPoint)
      )
  }
}
