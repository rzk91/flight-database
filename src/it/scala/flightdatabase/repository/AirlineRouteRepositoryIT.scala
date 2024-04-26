package flightdatabase.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.domain.EntryNotFound
import flightdatabase.domain.airline_route.AirlineRoute
import flightdatabase.domain.airline_route.AirlineRouteCreate
import flightdatabase.testutils.RepositoryCheck
import flightdatabase.testutils.implicits.enrichIOOperation
import org.scalatest.Inspectors.forAll

final class AirlineRouteRepositoryIT extends RepositoryCheck {

  lazy val repo: AirlineRouteRepository[IO] = AirlineRouteRepository.make[IO].unsafeRunSync()

  val originalAirlineRoutes: List[AirlineRoute] = List(
    AirlineRoute(1, 1, "LH754", 1, 2),
    AirlineRoute(2, 1, "LH755", 2, 1),
    AirlineRoute(3, 5, "EK565", 2, 3),
    AirlineRoute(4, 5, "EK566", 3, 2),
    AirlineRoute(5, 4, "EK47", 3, 1),
    AirlineRoute(6, 4, "EK46", 1, 3)
  )

  val idNotPresent: Long = 100
  val valueNotPresent: String = "NotPresent"
  val veryLongIdNotPresent: Long = 1039495454540034858L

  val newAirlineRoute: AirlineRouteCreate = AirlineRouteCreate(4, "EK48", 3, 1)
  val updatedRouteNumber: String = "EK49"
  val patchedRouteNumber: String = "EK50"

  "Checking if airline-route exists" should "return a valid result" in {
    def airlineRouteExists(id: Long): Boolean = repo.doesAirlineRouteExist(id).unsafeRunSync()
    forAll(originalAirlineRoutes.map(_.id))(airlineRouteExists(_) shouldBe true)
    airlineRouteExists(idNotPresent) shouldBe false
    airlineRouteExists(veryLongIdNotPresent) shouldBe false
  }

  "Selecting all airline routes" should "return the correct detailed list" in {
    val airlineRoutes = repo.getAirlineRoutes.value

    airlineRoutes should not be empty
    airlineRoutes should contain only (originalAirlineRoutes: _*)
  }

  "Selecting all airline routes with only route numbers" should "return the correct list" in {
    val routeNumbers = repo.getAirlineRoutesOnlyRoutes.value

    routeNumbers should not be empty
    routeNumbers should contain only (originalAirlineRoutes.map(_.route): _*)
  }

  "Selecting an airline route by ID" should "return the correct route" in {
    forAll(originalAirlineRoutes) { airlineRoute =>
      repo.getAirlineRoute(airlineRoute.id).value shouldBe airlineRoute
    }
    repo.getAirlineRoute(idNotPresent).error shouldBe EntryNotFound(idNotPresent)
    repo.getAirlineRoute(veryLongIdNotPresent).error shouldBe EntryNotFound(veryLongIdNotPresent)
  }
}
