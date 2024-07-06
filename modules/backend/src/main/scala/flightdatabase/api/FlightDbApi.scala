package flightdatabase.api

import cats.effect.Async
import cats.effect.Sync
import cats.implicits._
import doobie.Transactor
import flightdatabase.FlightDbTable
import flightdatabase.FlightDbTable._
import flightdatabase.api.endpoints._
import flightdatabase.config.Configuration.ApiConfig
import flightdatabase.extensions.kleisli._
import flightdatabase.repository._
import org.http4s.HttpApp
import org.http4s.HttpRoutes
import org.http4s.Uri
import org.http4s.Uri.Path
import org.http4s.server.Router
import org.http4s.server.middleware.Logger

class FlightDbApi[F[_]: Async] private (
  apiConfig: ApiConfig,
  repos: RepositoryContainer[F]
)(implicit transactor: Transactor[F]) {

  private val helloWorldEndpoints =
    HelloWorldEndpoints[F](HELLO_WORLD.prefix, apiConfig.flightDbBaseUri)

  private val airplaneEndpoints = AirplaneEndpoints[F](AIRPLANE.prefix, repos.airplaneRepository)
  private val airlineEndpoints = AirlineEndpoints[F](AIRLINE.prefix, repos.airlineRepository)

  private val airlineAirplaneEndpoints =
    AirlineAirplaneEndpoints[F](AIRLINE_AIRPLANE.prefix, repos.airlineAirplaneRepository)

  private val airlineCityEndpoints =
    AirlineCityEndpoints[F](AIRLINE_CITY.prefix, repos.airlineCityRepository)

  private val airlineRouteEndpoints =
    AirlineRouteEndpoints[F](AIRLINE_ROUTE.prefix, repos.airlineRouteRepository)

  private val airportEndpoints = AirportEndpoints[F](AIRPORT.prefix, repos.airportRepository)
  private val cityEndpoints = CityEndpoints[F](CITY.prefix, repos.cityRepository)
  private val countryEndpoints = CountryEndpoints[F](COUNTRY.prefix, repos.countryRepository)
  private val currencyEndpoints = CurrencyEndpoints[F](CURRENCY.prefix, repos.currencyRepository)
  private val languageEndpoints = LanguageEndpoints[F](LANGUAGE.prefix, repos.languageRepository)

  private val manufacturerEndpoints =
    ManufacturerEndpoints[F](MANUFACTURER.prefix, repos.manufacturerRepository)

  val flightDbServices: HttpRoutes[F] =
    List(
      helloWorldEndpoints,
      airplaneEndpoints,
      airlineEndpoints,
      airlineAirplaneEndpoints,
      airlineCityEndpoints,
      airlineRouteEndpoints,
      airportEndpoints,
      cityEndpoints,
      countryEndpoints,
      currencyEndpoints,
      languageEndpoints,
      manufacturerEndpoints
    ).foldLeft(HttpRoutes.empty[F])(_ <+> _.routes)

  def flightDbApp(): F[HttpApp[F]] = {
    val validEntryPoints = getValidEntryPoints(apiConfig.flightDbBaseUri)
    val app = Router(apiConfig.entryPoint -> flightDbServices)
      .orNotFoundIf(req => !validEntryPoints.exists(req.uri.path.startsWith)) // Not found for invalid entry points
      .orBadRequest                                                           // Bad request for invalid requests
    val logging = apiConfig.logging
    Sync[F].delay(
      if (logging.active) Logger.httpApp(logging.withHeaders, logging.withBody)(app) else app
    )
  }

  private def getValidEntryPoints(mainEntryPoint: Uri): IndexedSeq[Path] =
    FlightDbTable.values.map(p => mainEntryPoint.addPath(p.prefix.tail).path)
}

object FlightDbApi {

  def apply[F[_]: Async](
    apiConfig: ApiConfig,
    repositoryContainer: RepositoryContainer[F]
  )(implicit transactor: Transactor[F]): FlightDbApi[F] =
    new FlightDbApi(apiConfig, repositoryContainer)
}
