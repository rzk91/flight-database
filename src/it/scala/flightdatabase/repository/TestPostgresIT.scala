package flightdatabase.repository

import cats.effect.Async
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.implicits._
import flightdatabase.domain.city.CityModel
import flightdatabase.testutils.PostgreSqlContainerSpec
import org.scalatest.EitherValues._
import org.scalatest.matchers.should.Matchers

final class TestPostgresIT extends PostgreSqlContainerSpec[IO] with Matchers {

  implicit override val async: Async[IO] = IO.asyncForIO

  "PostgreSQL container" should "be able to run a simple select query" in {
    sql"SELECT 1".query[Int].unique.transact(transactor).unsafeRunSync() shouldBe 1
  }

  "PostgreSQL container" should "run a simple query against the Flight Database" in {
    val cityNames = getFieldList[CityModel, String]("name")
      .transact(transactor)
      .unsafeRunSync()
      .map(_.value)
      .value

    cityNames should not be empty
    (cityNames should contain).allOf("Bangalore", "Frankfurt am Main")
  }
}
