package flightdatabase.api.endpoints

import cats.data.{NonEmptyList => Nel}
import cats.effect.IO
import cats.syntax.foldable._
import doobie.Put
import doobie.Read
import flightdatabase.api.Operator
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryListEmpty
import flightdatabase.domain.EntryNotFound
import flightdatabase.domain.InvalidField
import flightdatabase.domain.LongType
import flightdatabase.domain.ResultOrder
import flightdatabase.domain.ValidatedSortAndLimit
import flightdatabase.domain.WrongOperator
import flightdatabase.domain.airline.Airline
import flightdatabase.domain.airline.AirlineAlgebra
import flightdatabase.testutils._
import flightdatabase.testutils.implicits._
import org.http4s.Status._
import org.http4s.circe.CirceEntityCodec._
import org.scalamock.function.StubFunction3
import org.scalamock.function.StubFunction5
import org.scalamock.scalatest.MockFactory
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpecLike
import org.scalatest.matchers.should.Matchers

final class AirlineEndpointsTest
    extends IOEndpointsSpec
    with AnyFeatureSpecLike
    with GivenWhenThen
    with Matchers
    with CustomMatchers
    with MockFactory {
  val mockAlgebra: AirlineAlgebra[IO] = stub[AirlineAlgebra[IO]]
  override val api: Endpoints[IO] = AirlineEndpoints[IO]("/airlines", mockAlgebra)

  val originalAirlines: Nel[Airline] = Nel.of(
    Airline(1, "Lufthansa", "LH", "DLH", "LUFTHANSA", 2),
    Airline(2, "Emirates", "EK", "UAE", "EMIRATES", 4)
  )

  Feature("Checking if an airline exists") {
    Scenario("An airline exists") {
      Given("an existing airline ID")
      (mockAlgebra.doesAirlineExist _).when(testId).returns(IO.pure(true))

      When("the airline is checked")
      val response = headResponse(createIdUri(testId))

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the method should be called only once")
      (mockAlgebra.doesAirlineExist _).verify(testId).once()
    }

    Scenario("An airline does not exist") {
      Given("a non-existing airline ID")
      (mockAlgebra.doesAirlineExist _).when(testId).returns(IO.pure(false))

      When("the airline is checked")
      val response = headResponse(createIdUri(testId))

      Then("a 404 status is returned")
      response.status shouldBe NotFound

      And("the method should be called only once")
      (mockAlgebra.doesAirlineExist _).verify(testId).once()
    }

    Scenario("An invalid airline ID") {
      Given("an invalid airline ID")

      When("the airline is checked")
      val response = headResponse(createIdUri(invalid))

      Then("a 404 status is returned")
      response.status shouldBe NotFound

      And("the method should not be called")
      (mockAlgebra.doesAirlineExist _).verify(*).never()
    }
  }

  Feature("Fetching airlines") {
    val emptySortAndLimit = ValidatedSortAndLimit.empty
    def mockAirlinesOnly[V]
      : StubFunction3[ValidatedSortAndLimit, String, Read[V], IO[ApiResult[Nel[V]]]] =
      mockAlgebra.getAirlinesOnly(_: ValidatedSortAndLimit, _: String)(_: Read[V])

    Scenario("Fetching all airlines") {
      Given("no query parameters")
      (mockAlgebra.getAirlines _).when(emptySortAndLimit).returns(originalAirlines.asResult[IO])

      When("all airlines are fetched")
      val response = getResponse()

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should contain all airlines")
      response.extract[Nel[Airline]] shouldBe originalAirlines

      And("the right method should be called only once")
      (mockAlgebra.getAirlines _).verify(emptySortAndLimit).once()
      mockAirlinesOnly[String].verify(*, *, *).never()
    }

    Scenario("Fetching only name field for all airlines") {
      val tableField = "airline.name"
      val onlyAirlineNames = originalAirlines.map(_.name)

      Given("query parameters to return only the name field")
      val query = "return-only=name"
      mockAirlinesOnly[String]
        .when(emptySortAndLimit, tableField, *)
        .returns(onlyAirlineNames.asResult[IO])

      When("all airlines are fetched")
      val response = getResponse(createQueryUri(query))

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should contain only the name field")
      response.extract[Nel[String]] shouldBe onlyAirlineNames

      And("the right method should be called only once")
      mockAirlinesOnly[String].verify(emptySortAndLimit, tableField, *).once()
      mockAirlinesOnly[Long].verify(*, *, *).never()
      (mockAlgebra.getAirlines _).verify(*).never()
    }

    Scenario("Fetching and sorting only name fields for all airlines") {
      val tableField = "airline.name"
      val sortByName = ValidatedSortAndLimit.sort(tableField)
      val airlineNamesSorted = originalAirlines.map(_.name).sorted

      Given("query parameters to return only the name field and sort by it")
      val query = "return-only=name&sort-by=name"
      mockAirlinesOnly[String]
        .when(sortByName, tableField, *)
        .returns(airlineNamesSorted.asResult[IO])

      When("all airlines are fetched")
      val response = getResponse(createQueryUri(query))

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should contain only the name field sorted")
      response.extract[Nel[String]] shouldBe airlineNamesSorted

      And("the right method should be called only once")
      mockAirlinesOnly[String].verify(sortByName, tableField, *).once()
      mockAirlinesOnly[BigDecimal].verify(*, *, *).never()
      (mockAlgebra.getAirlines _).verify(*).never()
    }

    Scenario("Fetching, sorting, and limiting only IATA fields for all airlines") {
      val readField = "airline.iata"
      val sortAndLimit = ValidatedSortAndLimit(
        sortBy = Some("airline.name"),
        order = Some(ResultOrder.Descending),
        limit = Some(1),
        offset = Some(1)
      )
      val airlineIataSorted = Nel.one(originalAirlines.map(_.iata).sorted.reverse.tail.head)

      Given("query parameters to return only IATA, sort by name in reverse, and take second result")
      val query = s"return-only=iata&sort-by=name&order=desc&limit=1&offset=1"
      mockAirlinesOnly[String]
        .when(sortAndLimit, readField, *)
        .returns(airlineIataSorted.asResult[IO])

      When("all airlines are fetched")
      val response = getResponse(createQueryUri(query))

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should contain only the IATA field sorted and limited")
      response.extract[Nel[String]] shouldBe airlineIataSorted

      And("the right method should be called only once")
      mockAirlinesOnly[String].verify(sortAndLimit, readField, *).once()
      mockAirlinesOnly[BigDecimal].verify(*, *, *).never()
      (mockAlgebra.getAirlines _).verify(*).never()
    }

    Scenario("An empty list is returned") {
      Given("no airlines in the database")
      (mockAlgebra.getAirlines _)
        .when(emptySortAndLimit)
        .returns(EntryListEmpty.elevate[IO, Nel[Airline]])

      When("all airlines are fetched")
      val response = getResponse()

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should indicate an empty list")
      response.string shouldBe EntryListEmpty.error

      And("the right method should be called only once")
      (mockAlgebra.getAirlines _).verify(emptySortAndLimit).once()
      mockAirlinesOnly[String].verify(*, *, *).never()
    }

    Scenario("An invalid return-only field is passed") {
      Given("an invalid return-only field")
      val query = s"return-only=$invalid"

      When("all airlines are fetched")
      val response = getResponse(createQueryUri(query))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the error message")
      response.string shouldBe InvalidField(invalid).error

      And("no algebra methods should be called")
      (mockAlgebra.getAirlines _).verify(*).never()
      mockAirlinesOnly[String].verify(*, *, *).never()
    }

    Scenario("Invalid sorting or limiting parameters are passed") {
      Given("invalid sorting or limiting parameters")
      val query = s"sort-by=$invalid&limit=-1"

      When("all airlines are fetched")
      val response = getResponse(createQueryUri(query))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("All invalid parameters must be mentioned in the response body")
      response.string should includeAllOf(
        query.split("[&=]").toIndexedSeq: _*
      )

      And("no algebra methods should be called")
      (mockAlgebra.getAirlines _).verify(*).never()
      mockAirlinesOnly[String].verify(*, *, *).never()
    }
  }

  Feature("Fetching an airline by ID") {
    Scenario("Fetching an existing airline") {
      Given("an existing airline ID")
      (mockAlgebra.getAirline _).when(testId).returns(originalAirlines.head.asResult[IO])

      When("the airline is fetched")
      val response = getResponse(createIdUri(testId))

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should contain the airline")
      response.extract[Airline] shouldBe originalAirlines.head

      And("the right method should be called only once")
      (mockAlgebra.getAirline _).verify(testId).once()
    }

    Scenario("Fetching a non-existing airline") {
      val entryNotFound = EntryNotFound(testId)

      Given("a non-existing airline ID")
      (mockAlgebra.getAirline _).when(testId).returns(entryNotFound.elevate[IO, Airline])

      When("the airline is fetched")
      val response = getResponse(createIdUri(testId))

      Then("a 404 status is returned")
      response.status shouldBe NotFound

      And("the response body should contain the error message")
      response.string shouldBe entryNotFound.error

      And("the right method should be called only once")
      (mockAlgebra.getAirline _).verify(testId).once()
    }

    Scenario("An invalid airline ID") {
      Given("an invalid airline ID")

      When("the airline is fetched")
      val response = getResponse(createIdUri(invalid))

      Then("a 404 status is returned")
      response.status shouldBe NotFound

      And("no algebra methods should be called")
      (mockAlgebra.getAirline _).verify(*).never()
    }
  }

  Feature("Fetching airlines by some field") {
    val emptySortAndLimit = ValidatedSortAndLimit.empty
    val path = Some("filter")
    def mockAirlinesBy[V]: StubFunction5[
      String,
      Nel[V],
      Operator,
      ValidatedSortAndLimit,
      Put[V],
      IO[ApiResult[Nel[Airline]]]
    ] =
      mockAlgebra.getAirlinesBy(
        _: String,
        _: Nel[V],
        _: Operator,
        _: ValidatedSortAndLimit
      )(_: Put[V])

    Scenario("Fetching airlines by IATA") {
      val field = "iata"
      val iata = "LH"
      val airlinesByIata = Nel.fromListUnsafe(originalAirlines.filter(_.iata == iata))

      Given("an IATA value")
      mockAirlinesBy[String]
        .when(field, Nel.one(iata), Operator.Equals, emptySortAndLimit, *)
        .returns(airlinesByIata.asResult[IO])

      When("the airlines are fetched")
      val query = s"field=$field&value=$iata"
      val response = getResponse(createQueryUri(query, path))

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should contain the airlines with the given IATA")
      response.extract[Nel[Airline]] shouldBe airlinesByIata

      And("the right method should be called only once")
      mockAirlinesBy[String]
        .verify(field, Nel.one(iata), Operator.Equals, emptySortAndLimit, *)
        .once()
      mockAirlinesBy[Long].verify(*, *, *, *, *).never()
    }

    Scenario("Fetching using the IN operator") {
      val field = "id"
      val ids = Nel.fromListUnsafe((1L to 5L).toList)
      val airlinesByIds = Nel.fromListUnsafe(originalAirlines.filter(a => ids.exists(_ == a.id)))

      Given("a list of IDs")
      mockAirlinesBy[Long]
        .when(field, ids, Operator.In, emptySortAndLimit, *)
        .returns(airlinesByIds.asResult[IO])

      When("the airlines are fetched")
      val query = s"field=$field&operator=in&value=${ids.mkString_(",")}"
      val response = getResponse(createQueryUri(query, path))

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should contain the airlines with the given IDs")
      response.extract[Nel[Airline]] shouldBe airlinesByIds

      And("the right method should be called only once")
      mockAirlinesBy[Long].verify(field, ids, Operator.In, emptySortAndLimit, *).once()
      mockAirlinesBy[String].verify(*, *, *, *, *).never()
    }

    Scenario("Fetching and sorting airlines by ICAO") {
      val field = "icao"
      val notIcaos = Nel.of("QTR", "IDG")
      val airlinesByIcao =
        Nel
          .fromListUnsafe(originalAirlines.filter(a => notIcaos.exists(_ != a.icao)))
          .sortBy(_.icao)
          .reverse
      val sortAndLimit = ValidatedSortAndLimit.sortDescending("airline.icao")

      Given("a list of ICAO values")
      mockAirlinesBy[String]
        .when(field, notIcaos, Operator.NotIn, sortAndLimit, *)
        .returns(airlinesByIcao.asResult[IO])

      When("the airlines are fetched")
      val query =
        s"field=$field&operator=not_in&value=${notIcaos.mkString_(",")}&sort-by=icao&order=desc"
      val response = getResponse(createQueryUri(query, path))

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should contain the airlines with the given ICAO sorted in reverse")
      response.extract[Nel[Airline]] shouldBe airlinesByIcao

      And("the right method should be called only once")
      mockAirlinesBy[String].verify(field, notIcaos, Operator.NotIn, sortAndLimit, *).once()
      mockAirlinesBy[Long].verify(*, *, *, *, *).never()
    }

    Scenario("Invalid field") {
      Given("an invalid field")
      val query = s"field=$invalid&value=1"

      When("the airlines are fetched")
      val response = getResponse(createQueryUri(query, path))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the error message")
      response.string shouldBe InvalidField(invalid).error

      And("no algebra methods should be called")
      mockAirlinesBy[String].verify(*, *, *, *, *).never()
      mockAirlinesBy[Long].verify(*, *, *, *, *).never()
    }

    Scenario("Empty field") {
      Given("an empty field")
      val query = "field=&value=1"

      When("the airlines are fetched")
      val response = getResponse(createQueryUri(query, path))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the error message")
      response.string shouldBe InvalidField("").error

      And("no algebra methods should be called")
      mockAirlinesBy[String].verify(*, *, *, *, *).never()
      mockAirlinesBy[Long].verify(*, *, *, *, *).never()
    }

    Scenario("Invalid operator") {
      Given("an invalid operator")
      val query = s"field=id&operator=$invalid&value=1"

      When("the airlines are fetched")
      val response = getResponse(createQueryUri(query, path))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the error message")
      response.string should include(invalid)

      And("no algebra methods should be called")
      mockAirlinesBy[String].verify(*, *, *, *, *).never()
      mockAirlinesBy[Long].verify(*, *, *, *, *).never()
    }

    Scenario("Invalid operator because of field type") {
      val field = "id"
      val invalidOperator = Operator.StartsWith

      Given("an invalid operator for the field type")
      val query = s"field=$field&operator=${invalidOperator.entryName}&value=1"

      When("the airlines are fetched")
      val response = getResponse(createQueryUri(query, path))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the error message")
      response.string shouldBe WrongOperator(invalidOperator, field, LongType).error

      And("no algebra methods should be called")
      mockAirlinesBy[String].verify(*, *, *, *, *).never()
      mockAirlinesBy[Long].verify(*, *, *, *, *).never()
    }

    Scenario("Invalid filter path") {
      Given("an invalid filter path")
      val invalidPath = Some(invalid)

      When("the airlines are fetched")
      val query = "field=id&value=1"
      val response = getResponse(createQueryUri(query, invalidPath))

      Then("a 404 status is returned")
      response.status shouldBe NotFound

      And("no algebra methods should be called")
      mockAirlinesBy[String].verify(*, *, *, *, *).never()
      mockAirlinesBy[Long].verify(*, *, *, *, *).never()
    }
  }
}
