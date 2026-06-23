package flightdatabase.api.config

import com.comcast.ip4s._
import flightdatabase.syntax.string._
import org.http4s.Uri
import org.http4s.Uri.Authority
import org.http4s.Uri.Path
import org.http4s.Uri.Scheme
import org.http4s.Uri.{Host => UriHost}

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
