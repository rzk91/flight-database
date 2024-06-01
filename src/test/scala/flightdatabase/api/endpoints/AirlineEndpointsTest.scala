package flightdatabase.api.endpoints

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.domain.airline.AirlineAlgebra
import org.http4s.HttpApp
import org.http4s.Method
import org.http4s.Request
import org.http4s.Status._
import org.http4s.Uri
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class AirlineEndpointsTest extends AnyFlatSpec with Matchers with MockFactory {
  val mockAirlineAlgebra: AirlineAlgebra[IO] = mock[AirlineAlgebra[IO]]

  val endpoints: HttpApp[IO] =
    AirlineEndpoints[IO]("/airlines", mockAirlineAlgebra).endpoints.orNotFound

  // Valid and invalid IDs
  val validId: Long = 1
  val idNotFound: Long = 1000

  "Checking if an airline exists" should "return the right response code" in {
    def getResponse(id: Long, exists: Boolean): Int = {
      (mockAirlineAlgebra.doesAirlineExist _).expects(id).returns(IO.pure(exists)).once()

      endpoints
        .run(Request(method = Method.HEAD, uri = Uri.unsafeFromString(s"/$id")))
        .unsafeRunSync()
        .status
        .code
    }

    getResponse(validId, exists = true) shouldBe Ok.code
    getResponse(idNotFound, exists = false) shouldBe NotFound.code
  }
}
