package flightdatabase.api

import cats.effect._
import cats.syntax.semigroupk._
import doobie.hikari.HikariTransactor
import flightdatabase.api.services._
import org.http4s.server.Router
import org.http4s.server.middleware.Logger
import org.http4s.{HttpApp, HttpRoutes, Uri}

class FlightDbApi[F[_]: Async](flightDbBaseUri: Uri)(
  implicit transactor: Resource[F, HikariTransactor[F]]
) {

  private val languageService = LanguageService[F]
  private val currencyService = CurrencyService[F]
  private val cityService = CityService[F]
  private val countryService = CountryService[F]
  private val airplaneService = AirplaneService[F]

  private val helloWorldService = HelloWorldService[F](flightDbBaseUri)

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

  def apply[F[_]: Async](flightDbBaseUri: Uri)(
    implicit transactor: Resource[F, HikariTransactor[F]]
  ): FlightDbApi[F] = new FlightDbApi(flightDbBaseUri)
}
