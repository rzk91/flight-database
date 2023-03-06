package flightdatabase.api

import cats.effect._
import cats.syntax.semigroupk._
import flightdatabase.api.services._
import org.http4s.server.Router
import org.http4s.server.middleware.Logger
import org.http4s.{HttpApp, HttpRoutes}

abstract class FlightDbApi[F[_]: Async] {

  private val languageService = LanguageService[F]
  private val cityService = CityService[F]
  private val airplaneService = AirplaneService[F]

  private val helloWorldService = HelloWorldService[F]

  // TODO: List is incomplete...
  val flightDbServices: HttpRoutes[F] =
    List(languageService, cityService, airplaneService).reduceLeft(_ <+> _)

  def flightDbApp(
    includeLogging: Boolean = false,
    withHeaders: Boolean = false,
    withBody: Boolean = false
  ): HttpApp[F] = {
    val app = Router("/hello" -> helloWorldService, "/flightdb" -> flightDbServices).orNotFound
    if (includeLogging) Logger.httpApp(withHeaders, withBody)(app) else app
  }
}

// For import using IO as effect monad
object FlightDbApi extends FlightDbApi[IO]