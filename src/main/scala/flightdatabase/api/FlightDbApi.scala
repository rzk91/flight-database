package flightdatabase.api

import cats.effect._
import cats.implicits._
import doobie.hikari.HikariTransactor
import flightdatabase.api.endpoints._
import flightdatabase.config.Configuration.ApiConfig
import org.http4s.HttpApp
import org.http4s.HttpRoutes
import org.http4s.server.Router
import org.http4s.server.middleware.Logger

class FlightDbApi[F[_]: Async](apiConfig: ApiConfig)(
  implicit transactor: Resource[F, HikariTransactor[F]]
) {

  private val helloWorldEndpoints = HelloWorldEndpoints[F]("/hello", apiConfig.flightDbBaseUri)
  private val languageEndpoints = LanguageEndpoints[F]("/languages")
  private val currencyEndpoints = CurrencyEndpoints[F]("/languages")
  private val cityEndpoints = CityEndpoints[F]("/cities")
  private val countryEndpoints = CountryEndpoints[F]("/countries")
  private val airplaneEndpoints = AirplaneEndpoints[F]("/airplanes")

  // TODO: List is incomplete...
  val flightDbServices: HttpRoutes[F] =
    List(
      helloWorldEndpoints,
      languageEndpoints,
      currencyEndpoints,
      cityEndpoints,
      countryEndpoints,
      airplaneEndpoints
    ).reduceLeft(
      _ <+> _
    )

  def flightDbApp(): F[HttpApp[F]] = {
    val app = Router(apiConfig.entryPoint -> flightDbServices).orNotFound
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
