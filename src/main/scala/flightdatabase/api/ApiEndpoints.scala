package flightdatabase.api

import cats.effect._
import doobie._
import doobie.implicits._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.circe._
import flightdatabase.db.DbMethods._
import flightdatabase.db.DbMain

object ApiEndpoints {

  def runQuery[A](query: ConnectionIO[A]): IO[A] = DbMain.xa.use(query.transact(_))

  implicit def listStringEncoder: EntityEncoder[IO, List[String]] = jsonEncoderOf[IO, List[String]]

  val helloWorldService = HttpRoutes.of[IO] {
    case GET -> Root / "hello" / name => Ok(s"Hello, $name")
  }

  val flightDbService = HttpRoutes.of[IO] {
    case GET -> Root / "countries" => runQuery(getCountryNames).flatMap(Ok(_))
    case GET -> Root / "cities" / "bycountry" / country =>
      runQuery(getCitiesFromCountry(country)).flatMap(Ok(_))
  }
}
