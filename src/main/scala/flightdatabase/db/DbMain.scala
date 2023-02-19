package flightdatabase.db

import cats.effect._
import doobie.util.ExecutionContexts
import flightdatabase.config.Configuration._
import flightdatabase.db.DbInitiation._
import flightdatabase.db.JsonToSqlConverter

object DbMain extends IOApp {

  val xa = for {
    ec <- ExecutionContexts.fixedThreadPool[IO](dbConfig.threadPoolSize)
    xa <- transactor(dbConfig, ec)
  } yield xa

  def run(args: List[String]): IO[ExitCode] = {
    if (setupConfig.createScripts) JsonToSqlConverter.setupScripts()

    xa.use(initialize).as(ExitCode.Success)
  }
}
