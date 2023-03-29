package flightdatabase.api

import cats.effect._
import cats.syntax.semigroupk._
import flightdatabase.api.services._
import org.http4s.server.Router
import org.http4s.server.middleware.Logger
import org.http4s.{HttpApp, HttpRoutes}

class FlightDbApi[F[_]: Async] {

  private val languageService = LanguageService[F]
  private val currencyService = CurrencyService[F]
  private val cityService = CityService[F]
  private val countryService = CountryService[F]
  private val airplaneService = AirplaneService[F]

  private val helloWorldService = HelloWorldService[F]

  // TODO: List is incomplete...
  val flightDbServices: HttpRoutes[F] =
    List(languageService, currencyService, cityService, countryService, airplaneService).reduceLeft(
      _ <+> _
    )

  def flightDbApp(
    includeLogging: Boolean = false,
    withHeaders: Boolean = false,
    withBody: Boolean = false
  ): HttpApp[F] = {
    val app = Router("/hello" -> helloWorldService, "/flightdb" -> flightDbServices).orNotFound
    if (includeLogging) Logger.httpApp(withHeaders, withBody)(app) else app
  }
}

object FlightDbApi {
  def apply[F[_]: Async]: FlightDbApi[F] = new FlightDbApi()
}
