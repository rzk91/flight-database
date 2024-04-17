package flightdatabase.api

import cats.effect.Async
import cats.effect.Sync
import cats.implicits._
import doobie.Transactor
import flightdatabase.api.endpoints._
import flightdatabase.config.Configuration.ApiConfig
import flightdatabase.repository._
import org.http4s.HttpApp
import org.http4s.HttpRoutes
import org.http4s.server.Router
import org.http4s.server.middleware.Logger

class FlightDbApi[F[_]: Async] private (
  apiConfig: ApiConfig,
  repos: RepositoryContainer[F]
)(implicit transactor: Transactor[F]) {

  private val helloWorldEndpoints = HelloWorldEndpoints[F]("/hello", apiConfig.flightDbBaseUri)
  private val airplaneEndpoints = AirplaneEndpoints[F]("/airplanes", repos.airplaneRepository)
  private val airportEndpoints = AirportEndpoints[F]("/airports", repos.airportRepository)
  private val cityEndpoints = CityEndpoints[F]("/cities", repos.cityRepository)
  private val countryEndpoints = CountryEndpoints[F]("/countries", repos.countryRepository)
  private val currencyEndpoints = CurrencyEndpoints[F]("/currencies", repos.currencyRepository)
  private val fleetEndpoints = FleetEndpoints[F]("/fleets", repos.fleetRepository)

  private val fleetAirplaneEndpoints =
    FleetAirplaneEndpoints[F]("/fleet-airplanes", repos.fleetAirplaneRepository)

  private val fleetRouteEndpoints =
    FleetRouteEndpoints[F]("/fleet-routes", repos.fleetRouteRepository)
  private val languageEndpoints = LanguageEndpoints[F]("/languages", repos.languageRepository)

  private val manufacturerEndpoints =
    ManufacturerEndpoints[F]("/manufacturers", repos.manufacturerRepository)

  val flightDbServices: HttpRoutes[F] =
    List(
      helloWorldEndpoints,
      airplaneEndpoints,
      airportEndpoints,
      cityEndpoints,
      countryEndpoints,
      currencyEndpoints,
      fleetEndpoints,
      fleetAirplaneEndpoints,
      fleetRouteEndpoints,
      languageEndpoints,
      manufacturerEndpoints
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
    repositoryContainer: RepositoryContainer[F]
  )(implicit transactor: Transactor[F]): FlightDbApi[F] =
    new FlightDbApi(apiConfig, repositoryContainer)
}
