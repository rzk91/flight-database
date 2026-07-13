package flightdatabase.api.endpoints

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.api.testutils.IOEndpointsSpec
import org.http4s.MediaType
import org.http4s.Status.NotFound
import org.http4s.Status.Ok
import org.http4s.Uri
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpecLike
import org.scalatest.matchers.should.Matchers

final class ApiDocsEndpointsTest
    extends IOEndpointsSpec
    with AnyFeatureSpecLike
    with GivenWhenThen
    with Matchers {

  override val api: Endpoints[IO] = ApiDocsEndpoints[IO]("/docs")

  Feature("Browsing the API reference") {

    Scenario("The reference shell") {
      Given("a request for the docs root")
      val uri = Uri.unsafeFromString("/")

      When("the shell is requested")
      val response = getResponse(uri)

      Then("a 200 status with an HTML body is returned")
      response.status shouldBe Ok
      response.contentType.map(_.mediaType) shouldBe Some(MediaType.text.html)

      And("the base-path template has been substituted")
      val body = response.as[String].unsafeRunSync()
      body should include("Scalar.createApiReference")
      (body should not).include("{{base}}")
    }

    Scenario("The raw OpenAPI spec") {
      Given("a request for the spec")
      val uri = Uri.unsafeFromString("/openapi.yaml")

      When("the spec is requested")
      val response = getResponse(uri)

      Then("a 200 status is returned")
      response.status shouldBe Ok
    }

    Scenario("A webjar asset") {
      Given("a request for a pinned Scalar webjar asset")
      val uri = Uri.unsafeFromString("/dist/browser/standalone.js")

      When("the asset is requested")
      val response = getResponse(uri)

      Then("a 200 status is returned")
      response.status shouldBe Ok
    }

    Scenario("A non-existing asset") {
      Given("a request for an asset that doesn't exist")
      val uri = Uri.unsafeFromString("/no-such-asset.js")

      When("the asset is requested")
      val response = getResponse(uri)

      Then("a 404 status is returned")
      response.status shouldBe NotFound
    }
  }
}
