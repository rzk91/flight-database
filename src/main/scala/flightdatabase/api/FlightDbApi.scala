package flightdatabase.api

import cats.effect.Async
import cats.effect.Sync
import cats.implicits._
import doobie.Transactor
import flightdatabase.api.endpoints._
import flightdatabase.config.Configuration.ApiConfig
import flightdatabase.domain.FlightDbTable._
import flightdatabase.repository._
import flightdatabase.utils.implicits.enrichKleisliResponse
import org.http4s.HttpApp
import org.http4s.HttpRoutes
import org.http4s.server.Router
import org.http4s.server.middleware.Logger

class FlightDbApi[F[_]: Async] private (
  apiConfig: ApiConfig,
  repos: RepositoryContainer[F]
)(implicit transactor: Transactor[F]) {

  private val prefixMap: Map[Table, String] = Map(
    AIRPLANE         -> "/airplanes",
    AIRLINE          -> "/airlines",
    AIRLINE_AIRPLANE -> "/airline-airplanes",
    AIRLINE_CITY     -> "/airline-cities",
    AIRLINE_ROUTE    -> "/airline-routes",
    AIRPORT          -> "/airports",
    CITY             -> "/cities",
    COUNTRY          -> "/countries",
    CURRENCY         -> "/currencies",
    LANGUAGE         -> "/languages",
    MANUFACTURER     -> "/manufacturers",
    HELLO_WORLD      -> "/hello"
  )

  private val helloWorldEndpoints =
    HelloWorldEndpoints[F](prefixMap(HELLO_WORLD), apiConfig.flightDbBaseUri)

  private val airplaneEndpoints =
    AirplaneEndpoints[F](prefixMap(AIRPLANE), repos.airplaneRepository)

  private val airlineEndpoints = AirlineEndpoints[F](prefixMap(AIRLINE), repos.airlineRepository)

  private val airlineAirplaneEndpoints =
    AirlineAirplaneEndpoints[F](prefixMap(AIRLINE_AIRPLANE), repos.airlineAirplaneRepository)

  private val airlineCityEndpoints =
    AirlineCityEndpoints[F](prefixMap(AIRLINE_CITY), repos.airlineCityRepository)

  private val airlineRouteEndpoints =
    AirlineRouteEndpoints[F](prefixMap(AIRLINE_ROUTE), repos.airlineRouteRepository)

  private val airportEndpoints = AirportEndpoints[F](prefixMap(AIRPORT), repos.airportRepository)
  private val cityEndpoints = CityEndpoints[F](prefixMap(CITY), repos.cityRepository)
  private val countryEndpoints = CountryEndpoints[F](prefixMap(COUNTRY), repos.countryRepository)

  private val currencyEndpoints =
    CurrencyEndpoints[F](prefixMap(CURRENCY), repos.currencyRepository)

  private val languageEndpoints =
    LanguageEndpoints[F](prefixMap(LANGUAGE), repos.languageRepository)

  private val manufacturerEndpoints =
    ManufacturerEndpoints[F](prefixMap(MANUFACTURER), repos.manufacturerRepository)

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
    val validEntryPoints = prefixMap.values.map(p => apiConfig.flightDbBaseUri.addPath(p.tail).path)
    val app = Router(apiConfig.entryPoint -> flightDbServices)
      .orNotFoundIf(req => !validEntryPoints.exists(req.uri.path.startsWith)) // Not found for invalid entry points
      .orBadRequest                                                           // Bad request for invalid requests
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
