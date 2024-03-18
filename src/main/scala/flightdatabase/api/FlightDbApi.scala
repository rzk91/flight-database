package flightdatabase.api

import cats.effect._
import cats.implicits._
import doobie.hikari.HikariTransactor
import flightdatabase.api.services._
import flightdatabase.config.Configuration.ApiConfig
import org.http4s.HttpApp
import org.http4s.HttpRoutes
import org.http4s.Uri
import org.http4s.server.Router
import org.http4s.server.middleware.Logger

class FlightDbApi[F[_]: Async](apiConfig: ApiConfig)(
  implicit transactor: Resource[F, HikariTransactor[F]]
) {

  private val languageService = LanguageService[F]
  private val currencyService = CurrencyService[F]
  private val cityService = CityService[F]
  private val countryService = CountryService[F]
  private val airplaneService = AirplaneService[F]

  private val helloWorldService = HelloWorldService[F](apiConfig.flightDbBaseUri)

  // TODO: List is incomplete...
  val flightDbServices: HttpRoutes[F] =
    List(languageService, currencyService, cityService, countryService, airplaneService).reduceLeft(
      _ <+> _
    )

  def flightDbApp(): F[HttpApp[F]] = {
    val app =
      Router("/hello" -> helloWorldService, s"/${apiConfig.entryPoint}" -> flightDbServices).orNotFound
    val logging = apiConfig.logging
    Sync[F].delay(
      if (logging.active) Logger.httpApp(logging.withHeaders, logging.withBody)(app) else app
    )
  }
}

object FlightDbApi {

  def apply[F[_]: Async](apiConfig: ApiConfig)(
    implicit transactor: Resource[F, HikariTransactor[F]]
  ): FlightDbApi[F] = new FlightDbApi(apiConfig)
}
