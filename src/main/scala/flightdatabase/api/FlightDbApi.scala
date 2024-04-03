package flightdatabase.api

import cats.effect._
import cats.implicits._
import doobie.hikari.HikariTransactor
import flightdatabase.api.endpoints._
import flightdatabase.config.Configuration.ApiConfig
import flightdatabase.repository._
import org.http4s.HttpApp
import org.http4s.HttpRoutes
import org.http4s.server.Router
import org.http4s.server.middleware.Logger

class FlightDbApi[F[_]: Async] private (
  apiConfig: ApiConfig,
  airplaneRepository: AirplaneRepository[F],
  cityRepository: CityRepository[F],
  countryRepository: CountryRepository[F],
  currencyRepository: CurrencyRepository[F],
  languageRepository: LanguageRepository[F]
)(
  implicit transactor: Resource[F, HikariTransactor[F]]
) {

  private val helloWorldEndpoints = HelloWorldEndpoints[F]("/hello", apiConfig.flightDbBaseUri)
  private val airplaneEndpoints = AirplaneEndpoints[F]("/airplanes", airplaneRepository)
  private val cityEndpoints = CityEndpoints[F]("/cities", cityRepository)
  private val countryEndpoints = CountryEndpoints[F]("/countries", countryRepository)
  private val currencyEndpoints = CurrencyEndpoints[F]("/currencies", currencyRepository)
  private val languageEndpoints = LanguageEndpoints[F]("/languages", languageRepository)

  // TODO: List is incomplete...
  val flightDbServices: HttpRoutes[F] =
    List(
      helloWorldEndpoints,
      airplaneEndpoints,
      cityEndpoints,
      countryEndpoints,
      currencyEndpoints,
      languageEndpoints
    ).foldLeft(HttpRoutes.empty[F])(_ <+> _.routes)

  def flightDbApp(): F[HttpApp[F]] = {
    val app = Router(apiConfig.entryPoint -> flightDbServices).orNotFound
    val logging = apiConfig.logging
    Sync[F].delay(
      if (logging.active) Logger.httpApp(logging.withHeaders, logging.withBody)(app) else app
    )
  }
}

object FlightDbApi {

  def apply[F[_]: Async](
    apiConfig: ApiConfig,
    airplaneRepository: AirplaneRepository[F],
    cityRepository: CityRepository[F],
    countryRepository: CountryRepository[F],
    currencyRepository: CurrencyRepository[F],
    languageRepository: LanguageRepository[F]
  )(
    implicit transactor: Resource[F, HikariTransactor[F]]
  ): FlightDbApi[F] =
    new FlightDbApi(
      apiConfig,
      airplaneRepository,
      cityRepository,
      countryRepository,
      currencyRepository,
      languageRepository
    )
}
