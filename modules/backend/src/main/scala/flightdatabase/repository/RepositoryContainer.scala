package flightdatabase.repository

import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.Transactor

class RepositoryContainer[F[_]: Concurrent] private (
  val airplaneRepository: AirplaneRepository[F],
  val airlineRepository: AirlineRepository[F],
  val airlineAirplaneRepository: AirlineAirplaneRepository[F],
  val airlineCityRepository: AirlineCityRepository[F],
  val airlineRouteRepository: AirlineRouteRepository[F],
  val airportRepository: AirportRepository[F],
  val cityRepository: CityRepository[F],
  val countryRepository: CountryRepository[F],
  val currencyRepository: CurrencyRepository[F],
  val languageRepository: LanguageRepository[F],
  val manufacturerRepository: ManufacturerRepository[F]
)(implicit transactor: Transactor[F])

object RepositoryContainer {

  def make[F[_]: Concurrent](implicit transactor: Transactor[F]): F[RepositoryContainer[F]] =
    for {
      airplaneRepository        <- AirplaneRepository.make[F]
      airlineRepository         <- AirlineRepository.make[F]
      airlineAirplaneRepository <- AirlineAirplaneRepository.make[F]
      airlineCityRepository     <- AirlineCityRepository.make[F]
      airlineRouteRepository    <- AirlineRouteRepository.make[F]
      airportRepository         <- AirportRepository.make[F]
      cityRepository            <- CityRepository.make[F]
      countryRepository         <- CountryRepository.make[F]
      currencyRepository        <- CurrencyRepository.make[F]
      languageRepository        <- LanguageRepository.make[F]
      manufacturerRepository    <- ManufacturerRepository.make[F]
    } yield new RepositoryContainer[F](
      airplaneRepository,
      airlineRepository,
      airlineAirplaneRepository,
      airlineCityRepository,
      airlineRouteRepository,
      airportRepository,
      cityRepository,
      countryRepository,
      currencyRepository,
      languageRepository,
      manufacturerRepository
    )

  def resource[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): Resource[F, RepositoryContainer[F]] =
    for {
      airplaneRepository        <- AirplaneRepository.resource[F]
      airlineRepository         <- AirlineRepository.resource[F]
      airlineAirplaneRepository <- AirlineAirplaneRepository.resource[F]
      airlineCityRepository     <- AirlineCityRepository.resource[F]
      airlineRouteRepository    <- AirlineRouteRepository.resource[F]
      airportRepository         <- AirportRepository.resource[F]
      cityRepository            <- CityRepository.resource[F]
      countryRepository         <- CountryRepository.resource[F]
      currencyRepository        <- CurrencyRepository.resource[F]
      languageRepository        <- LanguageRepository.resource[F]
      manufacturerRepository    <- ManufacturerRepository.resource[F]
    } yield new RepositoryContainer[F](
      airplaneRepository,
      airlineRepository,
      airlineAirplaneRepository,
      airlineCityRepository,
      airlineRouteRepository,
      airportRepository,
      cityRepository,
      countryRepository,
      currencyRepository,
      languageRepository,
      manufacturerRepository
    )
}
