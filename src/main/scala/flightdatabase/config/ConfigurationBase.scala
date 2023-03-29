package flightdatabase.config

import com.comcast.ip4s._
import flightdatabase.config.EnvironmentEnum.Env

trait ConfigurationBase {
  case class Environment(env: Env)

  case class Access(username: String, password: String)

  case class DatabaseConfig(
    driver: String,
    url: String,
    access: Access,
    baseline: String,
    threadPoolSize: Int
  )

  case class ApiConfig private (host: String, port: Int) {
    lazy val hostName: Option[Host] = Host.fromString(host)

    lazy val portNumber: Port =
      Port
        .fromInt(port)
        .getOrElse(
          throw new IllegalArgumentException(
            s"Port number $port is invalid. Must be an integer between 0 and 65535."
          )
        )
  }
}
