package flightdatabase.repository

import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.Transactor

class RepositoryContainer[F[_]: Concurrent] private (
  val airplaneRepository: AirplaneRepository[F],
  val airportRepository: AirportRepository[F],
  val cityRepository: CityRepository[F],
  val countryRepository: CountryRepository[F],
  val currencyRepository: CurrencyRepository[F],
  val fleetRepository: FleetRepository[F],
  val fleetAirplaneRepository: FleetAirplaneRepository[F],
  val fleetRouteRepository: FleetRouteRepository[F],
  val languageRepository: LanguageRepository[F],
  val manufacturerRepository: ManufacturerRepository[F]
)(implicit transactor: Transactor[F])

object RepositoryContainer {

  def make[F[_]: Concurrent](implicit transactor: Transactor[F]): F[RepositoryContainer[F]] =
    for {
      airplaneRepository      <- AirplaneRepository.make[F]
      airportRepository       <- AirportRepository.make[F]
      cityRepository          <- CityRepository.make[F]
      countryRepository       <- CountryRepository.make[F]
      currencyRepository      <- CurrencyRepository.make[F]
      fleetRepository         <- FleetRepository.make[F]
      fleetAirplaneRepository <- FleetAirplaneRepository.make[F]
      fleetRouteRepository    <- FleetRouteRepository.make[F]
      languageRepository      <- LanguageRepository.make[F]
      manufacturerRepository  <- ManufacturerRepository.make[F]
    } yield new RepositoryContainer[F](
      airplaneRepository,
      airportRepository,
      cityRepository,
      countryRepository,
      currencyRepository,
      fleetRepository,
      fleetAirplaneRepository,
      fleetRouteRepository,
      languageRepository,
      manufacturerRepository
    )

  def resource[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): Resource[F, RepositoryContainer[F]] =
    for {
      airplaneRepository      <- AirplaneRepository.resource[F]
      airportRepository       <- AirportRepository.resource[F]
      cityRepository          <- CityRepository.resource[F]
      countryRepository       <- CountryRepository.resource[F]
      currencyRepository      <- CurrencyRepository.resource[F]
      fleetRepository         <- FleetRepository.resource[F]
      fleetAirplaneRepository <- FleetAirplaneRepository.resource[F]
      fleetRouteRepository    <- FleetRouteRepository.resource[F]
      languageRepository      <- LanguageRepository.resource[F]
      manufacturerRepository  <- ManufacturerRepository.resource[F]
    } yield new RepositoryContainer[F](
      airplaneRepository,
      airportRepository,
      cityRepository,
      countryRepository,
      currencyRepository,
      fleetRepository,
      fleetAirplaneRepository,
      fleetRouteRepository,
      languageRepository,
      manufacturerRepository
    )
}
