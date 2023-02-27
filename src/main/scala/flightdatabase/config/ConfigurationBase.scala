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
    threadPoolSize: Int,
    cleanDatabase: Boolean
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
