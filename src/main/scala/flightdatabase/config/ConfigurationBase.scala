package flightdatabase.config

import pureconfig.ConfigSource
import org.http4s.headers.Forwarded
import com.comcast.ip4s._

trait ConfigurationBase {
  case class SetupConfig(createScripts: Boolean, cleanDatabase: Boolean)

  case class Access(username: String, password: String)

  case class DatabaseConfig(
    driver: String,
    url: String,
    access: Access,
    baseline: String,
    threadPoolSize: Int
  )

  case class ApiConfig(host: String, port: Int) {
    def hostName: Option[Host] = Host.fromString(host)

    def portNumber: Port =
      Port
        .fromInt(port)
        .getOrElse(
          throw new IllegalArgumentException(
            s"Port number $port is invalid. Must be an integer between 0 and 65535."
          )
        )
  }
}
