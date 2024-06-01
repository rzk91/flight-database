package flightdatabase.api.endpoints

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.config.Configuration
import flightdatabase.domain.airline.AirlineAlgebra
import org.http4s.HttpApp
import org.http4s.Method
import org.http4s.Request
import org.http4s.Uri
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class AirlineEndpointsTest extends AnyFlatSpec with Matchers with MockFactory {
  val baseUri = Configuration.configUnsafe.apiConfig.flightDbBaseUri
  val prefix = "/airlines"
  val mockAirlineAlgebra: AirlineAlgebra[IO] = mock[AirlineAlgebra[IO]]
  val endpoints: HttpApp[IO] = AirlineEndpoints[IO](prefix, mockAirlineAlgebra).endpoints.orNotFound

  // Valid and invalid IDs
  val validId: Long = 1
  val idNotFound: Long = 1000

  // Status codes
  val ok: Int = 200
  val created: Int = 201
  val noContent: Int = 204
  val badRequest: Int = 400
  val notFound: Int = 404
  val conflict: Int = 409

  "Creating a URI string" should "work properly" in {
    createUri("1").toString shouldBe s"$baseUri$prefix/1"
  }

  "Checking if an airline exists" should "return 200 for a successful call" in {
    (mockAirlineAlgebra.doesAirlineExist _).expects(validId).returns(IO.pure(true)).once()

    val response = endpoints
      .run(Request(method = Method.HEAD, uri = createUri(validId.toString)))
      .unsafeRunSync()

    response.status.code shouldBe ok
  }

  it should "return 404 for a failed call" in {
    (mockAirlineAlgebra.doesAirlineExist _).expects(idNotFound).returns(IO.pure(false)).once()

    val response = endpoints
      .run(Request(method = Method.HEAD, uri = createUri(idNotFound.toString)))
      .unsafeRunSync()

    response.status.code shouldBe notFound
  }

  private def createUri(suffix: String): Uri = Uri.unsafeFromString(s"$baseUri$prefix/$suffix")
}
