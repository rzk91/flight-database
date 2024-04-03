package flightdatabase

import cats.effect._
import doobie.hikari.HikariTransactor
import flightdatabase.api.FlightDbApi
import flightdatabase.config.Configuration
import flightdatabase.db.Database
import flightdatabase.repository._

object FlightDbMain extends IOApp.Simple {

  override def run: IO[Unit] = {
    for {
      conf <- Configuration.configAsResource[IO]
      _    <- Database.initialise[IO](conf.dbConfig, conf.cleanDatabase)
      // Implicit resource-based HikariTransactor for better connection pooling
      implicit0(xa: Resource[IO, HikariTransactor[IO]]) = Database.transactor[IO](conf.dbConfig)
      port <- Resource.eval(IO.fromEither(conf.apiConfig.portNumber))
      // TODO: Think of a better way to pass these repositories to `httpApp`
      airplaneRepo <- AirplaneRepository.resource[IO]
      cityRepo     <- CityRepository.resource[IO]
      countryRepo  <- CountryRepository.resource[IO]
      currencyRepo <- CurrencyRepository.resource[IO]
      languageRepo <- LanguageRepository.resource[IO]
      httpApp <- Resource.eval(
        FlightDbApi[IO](
          conf.apiConfig,
          airplaneRepo,
          cityRepo,
          countryRepo,
          currencyRepo,
          languageRepo
        ).flightDbApp()
      )
      _ <- Server.start(conf.apiConfig.hostName, port, httpApp)
    } yield ()
  }.useForever
}
