package flightdatabase.api.endpoints

import cats.data.{NonEmptyList => Nel}
import cats.effect.IO
import cats.syntax.foldable._
import doobie.Put
import doobie.Read
import flightdatabase.api.Operator
import flightdatabase.domain._
import flightdatabase.domain.airline.Airline
import flightdatabase.domain.airline.AirlineAlgebra
import flightdatabase.domain.airline.AirlinePatch
import flightdatabase.domain.partial.PartiallyAppliedGetAll
import flightdatabase.domain.partial.PartiallyAppliedGetBy
import flightdatabase.testutils._
import flightdatabase.testutils.implicits._
import io.circe.generic.extras.ConfiguredJsonCodec
import org.http4s.Status.{Created => CreatedStatus, _}
import org.http4s.circe.CirceEntityCodec._
import org.scalamock.function.StubFunction1
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

  val mockGetAll: PartiallyAppliedGetAll[IO, Airline] = stub[PartiallyAppliedGetAll[IO, Airline]]
  val mockGetBy: PartiallyAppliedGetBy[IO, Airline] = stub[PartiallyAppliedGetBy[IO, Airline]]

  val originalAirlines: Nel[Airline] = Nel.of(
    Airline(1, "Lufthansa", "LH", "DLH", "LUFTHANSA", 2),
    Airline(2, "Emirates", "EK", "UAE", "EMIRATES", 4)
  )

  @ConfiguredJsonCodec case class InvalidAirline(name: String, iata: String, icao: String)

  case class CountryTest(name: String, iso2: String, code: Int)

  // ID -> CountryTest(Name, ISO2, Country [Phone] Code)
  val countryIdMap: Map[Long, CountryTest] = Map(
    2L -> CountryTest("Germany", "DE", 49),
    4L -> CountryTest("United Arab Emirates", "AE", 971)
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

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the right error message")
      response.string shouldBe EntryInvalidFormat.error

      And("the method should not be called")
      (mockAlgebra.doesAirlineExist _).verify(*).never()
    }
  }

  Feature("Fetching airlines") {
    val emptySortAndLimit = ValidatedSortAndLimit.empty
    val mockAirlines: StubFunction1[ValidatedSortAndLimit, IO[ApiResult[Nel[Airline]]]] =
      mockGetAll.apply(_: ValidatedSortAndLimit)
    def mockAirlinesOnly[V]
      : StubFunction3[ValidatedSortAndLimit, String, Read[V], IO[ApiResult[Nel[V]]]] =
      mockGetAll.apply(_: ValidatedSortAndLimit, _: String)(_: Read[V])

    Scenario("Fetching all airlines") {
      (() => mockAlgebra.getAirlines).when().returns(mockGetAll)

      Given("no query parameters")
      mockAirlines.when(emptySortAndLimit).returns(Got(originalAirlines).elevate[IO])

      When("all airlines are fetched")
      val response = getResponse()

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should contain all airlines")
      response.extract[Nel[Airline]] shouldBe originalAirlines

      And("the right method should be called only once")
      mockAirlines.verify(emptySortAndLimit).once()
      mockAirlinesOnly[String].verify(*, *, *).never()
    }

    Scenario("Fetching only name field for all airlines") {
      val tableField = "airline.name"
      val onlyAirlineNames = originalAirlines.map(_.name)
      (() => mockAlgebra.getAirlines).when().returns(mockGetAll)

      Given("query parameters to return only the name field")
      val query = "return-only=name"
      mockAirlinesOnly[String]
        .when(emptySortAndLimit, tableField, *)
        .returns(Got(onlyAirlineNames).elevate[IO])

      When("all airlines are fetched")
      val response = getResponse(createQueryUri(query))

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should contain only the name field")
      response.extract[Nel[String]] shouldBe onlyAirlineNames

      And("the right method should be called only once")
      mockAirlinesOnly[String].verify(emptySortAndLimit, tableField, *).once()
      mockAirlinesOnly[Long].verify(*, *, *).never()
      mockAirlines.verify(*).never()
    }

    Scenario("Fetching and sorting only name fields for all airlines") {
      val tableField = "airline.name"
      val sortByName = ValidatedSortAndLimit.sort(tableField)
      val airlineNamesSorted = originalAirlines.map(_.name).sorted
      (() => mockAlgebra.getAirlines).when().returns(mockGetAll)

      Given("query parameters to return only the name field and sort by it")
      val query = "return-only=name&sort-by=name"
      mockAirlinesOnly[String]
        .when(sortByName, tableField, *)
        .returns(Got(airlineNamesSorted).elevate[IO])

      When("all airlines are fetched")
      val response = getResponse(createQueryUri(query))

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should contain only the name field sorted")
      response.extract[Nel[String]] shouldBe airlineNamesSorted

      And("the right method should be called only once")
      mockAirlinesOnly[String].verify(sortByName, tableField, *).once()
      mockAirlinesOnly[BigDecimal].verify(*, *, *).never()
      mockAirlines.verify(*).never()
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
      (() => mockAlgebra.getAirlines).when().returns(mockGetAll)

      Given("query parameters to return only IATA, sort by name in reverse, and take second result")
      val query = s"return-only=iata&sort-by=name&order=desc&limit=1&offset=1"
      mockAirlinesOnly[String]
        .when(sortAndLimit, readField, *)
        .returns(Got(airlineIataSorted).elevate[IO])

      When("all airlines are fetched")
      val response = getResponse(createQueryUri(query))

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should contain only the IATA field sorted and limited")
      response.extract[Nel[String]] shouldBe airlineIataSorted

      And("the right method should be called only once")
      mockAirlinesOnly[String].verify(sortAndLimit, readField, *).once()
      mockAirlinesOnly[BigDecimal].verify(*, *, *).never()
      mockAirlines.verify(*).never()
    }

    Scenario("An empty list is returned") {
      (() => mockAlgebra.getAirlines).when().returns(mockGetAll)

      Given("no airlines in the database")
      mockAirlines.when(emptySortAndLimit).returns(EntryListEmpty.elevate[IO, Nel[Airline]])

      When("all airlines are fetched")
      val response = getResponse()

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should indicate an empty list")
      response.string shouldBe EntryListEmpty.error

      And("the right method should be called only once")
      mockAirlines.verify(emptySortAndLimit).once()
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
      mockAirlines.verify(*).never()
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
      mockAirlines.verify(*).never()
      mockAirlinesOnly[String].verify(*, *, *).never()
    }
  }

  Feature("Fetching an airline by ID") {
    Scenario("Fetching an existing airline") {
      Given("an existing airline ID")
      (mockAlgebra.getAirline _).when(testId).returns(Got(originalAirlines.head).elevate[IO])

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

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the right error message")
      response.string shouldBe EntryInvalidFormat.error

      And("no algebra methods should be called")
      (mockAlgebra.getAirline _).verify(*).never()
    }
  }

  Feature("Fetching airlines by some airline field") {
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
      mockGetBy.apply(
        _: String,
        _: Nel[V],
        _: Operator,
        _: ValidatedSortAndLimit
      )(_: Put[V])

    Scenario("Fetching airlines by IATA") {
      val field = "iata"
      val iata = "LH"
      val airlinesByIata = Nel.fromListUnsafe(originalAirlines.filter(_.iata == iata))
      (() => mockAlgebra.getAirlinesBy).when().returns(mockGetBy)

      Given("an IATA value")
      mockAirlinesBy[String]
        .when(field, Nel.one(iata), Operator.Equals, emptySortAndLimit, *)
        .returns(Got(airlinesByIata).elevate[IO])

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
      (() => mockAlgebra.getAirlinesBy).when().returns(mockGetBy)

      Given("a list of IDs")
      mockAirlinesBy[Long]
        .when(field, ids, Operator.In, emptySortAndLimit, *)
        .returns(Got(airlinesByIds).elevate[IO])

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
      (() => mockAlgebra.getAirlinesBy).when().returns(mockGetBy)

      Given("a list of ICAO values")
      mockAirlinesBy[String]
        .when(field, notIcaos, Operator.NotIn, sortAndLimit, *)
        .returns(Got(airlinesByIcao).elevate[IO])

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

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the right error message")
      response.string shouldBe EntryInvalidFormat.error

      And("no algebra methods should be called")
      mockAirlinesBy[String].verify(*, *, *, *, *).never()
      mockAirlinesBy[Long].verify(*, *, *, *, *).never()
    }

    Scenario("No query parameters passed") {
      Given("no query parameters")
      val query = ""

      When("the airlines are fetched")
      val response = getResponse(createQueryUri(query, path))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the right error message")
      response.string shouldBe EntryInvalidFormat.error

      And("no algebra methods should be called")
      mockAirlinesBy[String].verify(*, *, *, *, *).never()
      mockAirlinesBy[Long].verify(*, *, *, *, *).never()
    }
  }

  Feature("Fetching airlines by a country field") {
    val emptySortAndLimit = ValidatedSortAndLimit.empty
    val path = Some("country/filter")
    def mockAirlinesByCountry[V]: StubFunction5[
      String,
      Nel[V],
      Operator,
      ValidatedSortAndLimit,
      Put[V],
      IO[ApiResult[Nel[Airline]]]
    ] =
      mockGetBy.apply(
        _: String,
        _: Nel[V],
        _: Operator,
        _: ValidatedSortAndLimit
      )(_: Put[V])

    Scenario("Fetching airlines by country name") {
      val field = "name"
      val country = "Germany"
      val airlinesByCountry = Nel.fromListUnsafe(
        originalAirlines.filter(a => countryIdMap(a.countryId).name == country)
      )
      (() => mockAlgebra.getAirlinesByCountry).when().returns(mockGetBy)

      Given("a country name")
      mockAirlinesByCountry[String]
        .when(field, Nel.one(country), Operator.Equals, emptySortAndLimit, *)
        .returns(Got(airlinesByCountry).elevate[IO])

      When("the airlines are fetched")
      val query = s"field=$field&value=$country"
      val response = getResponse(createQueryUri(query, path))

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should contain the airlines with the given country")
      response.extract[Nel[Airline]] shouldBe airlinesByCountry

      And("the right method should be called only once")
      mockAirlinesByCountry[String]
        .verify(field, Nel.one(country), Operator.Equals, emptySortAndLimit, *)
        .once()
      mockAirlinesByCountry[Long].verify(*, *, *, *, *).never()
    }

    Scenario("Fetching and sorting airlines by country ISO") {
      val field = "iso2"
      val isos = Nel.of("AE", "DE")
      val airlinesByIso =
        Nel
          .fromListUnsafe(
            originalAirlines.filter(a => isos.exists(_ == countryIdMap(a.countryId).iso2))
          )
          .sortBy(a => countryIdMap(a.countryId).iso2)
          .reverse
      val sortAndLimit = ValidatedSortAndLimit.sortDescending("country.iso2")
      (() => mockAlgebra.getAirlinesByCountry).when().returns(mockGetBy)

      Given("a list of ISO values")
      mockAirlinesByCountry[String]
        .when(field, isos, Operator.In, sortAndLimit, *)
        .returns(Got(airlinesByIso).elevate[IO])

      When("the airlines are fetched")
      val query = s"field=$field&operator=in&value=${isos.mkString_(",")}&sort-by=iso2&order=desc"
      val response = getResponse(createQueryUri(query, path))

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should contain the airlines for given country ISO2 sorted in reverse")
      response.extract[Nel[Airline]] shouldBe airlinesByIso

      And("the right method should be called only once")
      mockAirlinesByCountry[String].verify(field, isos, Operator.In, sortAndLimit, *).once()
      mockAirlinesByCountry[Long].verify(*, *, *, *, *).never()
    }

    Scenario("Fetching airlines by country code") {
      val field = "country_code"
      val code = 49
      val airlinesByCode = Nel.fromListUnsafe(
        originalAirlines.filter(a => countryIdMap(a.countryId).code == code)
      )
      (() => mockAlgebra.getAirlinesByCountry).when().returns(mockGetBy)

      Given("a country code")
      mockAirlinesByCountry[Int]
        .when(field, Nel.one(code), Operator.Equals, emptySortAndLimit, *)
        .returns(Got(airlinesByCode).elevate[IO])

      When("the airlines are fetched")
      val query = s"field=$field&value=$code"
      val response = getResponse(createQueryUri(query, path))

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should contain the airlines with the given country code")
      response.extract[Nel[Airline]] shouldBe airlinesByCode

      And("the right method should be called only once")
      mockAirlinesByCountry[Int]
        .verify(field, Nel.one(code), Operator.Equals, emptySortAndLimit, *)
        .once()
      mockAirlinesByCountry[String].verify(*, *, *, *, *).never()
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
      mockAirlinesByCountry[String].verify(*, *, *, *, *).never()
      mockAirlinesByCountry[Int].verify(*, *, *, *, *).never()
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
      mockAirlinesByCountry[String].verify(*, *, *, *, *).never()
      mockAirlinesByCountry[Int].verify(*, *, *, *, *).never()
    }

    Scenario("Invalid operator") {
      Given("an invalid operator")
      val query = s"field=name&operator=$invalid&value=1"

      When("the airlines are fetched")
      val response = getResponse(createQueryUri(query, path))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the error message")
      response.string should include(invalid)

      And("no algebra methods should be called")
      mockAirlinesByCountry[String].verify(*, *, *, *, *).never()
      mockAirlinesByCountry[Int].verify(*, *, *, *, *).never()
    }

    Scenario("Invalid operator because of field type") {
      val field = "name"
      val invalidOperator = Operator.GreaterThan

      Given("an invalid operator for the field type")
      val query = s"field=$field&operator=${invalidOperator.entryName}&value=1"

      When("the airlines are fetched")
      val response = getResponse(createQueryUri(query, path))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the error message")
      response.string shouldBe WrongOperator(invalidOperator, field, StringType).error

      And("no algebra methods should be called")
      mockAirlinesByCountry[String].verify(*, *, *, *, *).never()
      mockAirlinesByCountry[Int].verify(*, *, *, *, *).never()
    }

    Scenario("Invalid filter path") {
      Given("an invalid filter path")
      val invalidPath = Some(s"country/$invalid")

      When("the airlines are fetched")
      val query = "field=name&value=1"
      val response = getResponse(createQueryUri(query, invalidPath))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the right error message")
      response.string shouldBe EntryInvalidFormat.error

      And("no algebra methods should be called")
      mockAirlinesByCountry[String].verify(*, *, *, *, *).never()
      mockAirlinesByCountry[Int].verify(*, *, *, *, *).never()
    }

    Scenario("No query parameters passed") {
      Given("no query parameters")
      val query = ""

      When("the airlines are fetched")
      val response = getResponse(createQueryUri(query, path))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the right error message")
      response.string shouldBe EntryInvalidFormat.error

      And("no algebra methods should be called")
      mockAirlinesByCountry[String].verify(*, *, *, *, *).never()
      mockAirlinesByCountry[Int].verify(*, *, *, *, *).never()
    }
  }

  Feature("Creating an airline") {
    Scenario("A valid airline is created") {
      Given("a valid airline")
      val airline = originalAirlines.head
      val create = airline.toCreate
      (mockAlgebra.createAirline _).when(create).returns(Created(testId).elevate[IO])

      When("the airline is created")
      val response = postResponse(create)

      Then("a 201 status is returned")
      response.status shouldBe CreatedStatus

      And("the response body should only contain the created airline's ID")
      response.extract[Long] shouldBe testId

      And("the create method should be called only once")
      (mockAlgebra.createAirline _).verify(create).once()
    }

    Scenario("An invalid airline is created") {
      Given("an invalid airline")
      val invalidAirline = InvalidAirline("", "", "")

      When("the airline is created")
      val response = postResponse(invalidAirline)

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the error message")
      response.string shouldBe EntryInvalidFormat.error

      And("no algebra methods should be called")
      (mockAlgebra.createAirline _).verify(*).never()
    }

    Scenario("An already existing airline is created") {
      Given("an existing airline")
      val airline = originalAirlines.head.toCreate
      (mockAlgebra.createAirline _).when(airline).returns(EntryAlreadyExists.elevate[IO, Long])

      When("the airline is created")
      val response = postResponse(airline)

      Then("a 409 status is returned")
      response.status shouldBe Conflict

      And("the response body should contain the error message")
      response.string shouldBe EntryAlreadyExists.error

      And("the create method should be called only once")
      (mockAlgebra.createAirline _).verify(airline).once()
    }
  }

  Feature("Updating an airline") {
    val airline = originalAirlines.head
    val update = airline.toCreate

    Scenario("A valid airline is updated properly") {
      Given("an existing airline ID and a valid airline")
      (mockAlgebra.updateAirline _).when(airline).returns(Updated(testId).elevate[IO])

      When("the airline is updated")
      val response = putResponse(update, createIdUri(testId))

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should only contain the updated airline's ID")
      response.extract[Long] shouldBe testId

      And("the update method should be called only once")
      (mockAlgebra.updateAirline _).verify(airline).once()
    }

    Scenario("An invalid update is attempted") {
      Given("an invalid airline")
      val invalidAirline = InvalidAirline("", "", "")

      When("the airline is updated")
      val response = putResponse(invalidAirline, createIdUri(testId))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the error message")
      response.string shouldBe EntryInvalidFormat.error

      And("no algebra methods should be called")
      (mockAlgebra.updateAirline _).verify(*).never()
    }

    Scenario("An invalid ID is passed") {
      Given("an invalid airline ID")

      When("the airline is updated")
      val response = putResponse(update, createIdUri(invalid))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the error message")
      response.string shouldBe EntryInvalidFormat.error

      And("no algebra methods should be called")
      (mockAlgebra.updateAirline _).verify(*).never()
    }

    Scenario("Inconsistent IDs are passed") {
      Given("an inconsistent airline ID")
      val invalidId = testId + 1
      val airlineWithInconsitentId = update.copy(id = Some(invalidId))

      When("the airline is updated")
      val response = putResponse(airlineWithInconsitentId, createIdUri(testId))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the error message")
      response.string shouldBe InconsistentIds(testId, invalidId).error

      And("no algebra methods should be called")
      (mockAlgebra.updateAirline _).verify(*).never()
    }

    Scenario("A non-existing country ID is passed") {
      Given("a non-existing country ID")
      val airlineWithInvalidCountryId = update.copy(countryId = 1000)
      val newAirline = Airline.fromCreate(testId, airlineWithInvalidCountryId)
      (mockAlgebra.updateAirline _)
        .when(newAirline)
        .returns(EntryHasInvalidForeignKey.elevate[IO, Long])

      When("the airline is updated")
      val response = putResponse(airlineWithInvalidCountryId, createIdUri(testId))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the error message")
      response.string shouldBe EntryHasInvalidForeignKey.error

      And("the update method should be called only once")
      (mockAlgebra.updateAirline _).verify(newAirline).once()
    }
  }

  Feature("Partially updating an airline") {
    val airline = originalAirlines.head.copy(id = testId)
    val patch = AirlinePatch(name = Some(airline.name))

    Scenario("A valid patch is passed") {
      Given("an existing airline ID and a valid patch")
      (mockAlgebra.partiallyUpdateAirline _)
        .when(testId, patch)
        .returns(Updated(airline).elevate[IO])

      When("the airline is partially updated")
      val response = patchResponse(patch, createIdUri(testId))

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should contain the updated airline")
      response.extract[Airline] shouldBe airline

      And("the patch method should be called only once")
      (mockAlgebra.partiallyUpdateAirline _).verify(testId, patch).once()
    }

    Scenario("An invalid patch is passed") {
      Given("an invalid patch")

      When("the airline is partially updated")
      val response = patchResponse(invalid, createIdUri(testId))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the error message")
      response.string shouldBe EntryInvalidFormat.error

      And("no algebra methods should be called")
      (mockAlgebra.partiallyUpdateAirline _).verify(*, *).never()
    }

    Scenario("An invalid ID is passed") {
      Given("an invalid airline ID")

      When("the airline is partially updated")
      val response = patchResponse(patch, createIdUri(invalid))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the error message")
      response.string shouldBe EntryInvalidFormat.error

      And("no algebra methods should be called")
      (mockAlgebra.partiallyUpdateAirline _).verify(*, *).never()
    }

    Scenario("An invalid airline name is passed") {
      Given("an invalid airline name")
      val invalidPatch = AirlinePatch(name = Some(""))
      (mockAlgebra.partiallyUpdateAirline _)
        .when(testId, invalidPatch)
        .returns(EntryCheckFailed.elevate[IO, Airline])

      When("the airline is partially updated")
      val response = patchResponse(invalidPatch, createIdUri(testId))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the error message")
      response.string shouldBe EntryCheckFailed.error

      And("the patch method should be called only once")
      (mockAlgebra.partiallyUpdateAirline _).verify(testId, invalidPatch).once()
    }
  }

  Feature("Deleting an airline") {
    Scenario("An existing airline is deleted") {
      Given("an existing airline ID")
      (mockAlgebra.removeAirline _).when(testId).returns(Deleted.elevate[IO])

      When("the airline is deleted")
      val response = deleteResponse(createIdUri(testId))

      Then("a 204 status is returned")
      response.status shouldBe NoContent

      And("the response body should be empty")
      response.string shouldBe empty

      And("the delete method should be called only once")
      (mockAlgebra.removeAirline _).verify(testId).once()
    }

    Scenario("An invalid ID is passed") {
      Given("an invalid ID")

      When("the airline is deleted")
      val response = deleteResponse(createIdUri(invalid))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the error message")
      response.string shouldBe EntryInvalidFormat.error

      And("no algebra methods should be called")
      (mockAlgebra.removeAirline _).verify(*).never()
    }

    Scenario("A non-existing ID is passed") {
      Given("a non-existing ID")
      (mockAlgebra.removeAirline _).when(testId).returns(EntryNotFound(testId).elevate[IO, Unit])

      When("the airline is deleted")
      val response = deleteResponse(createIdUri(testId))

      Then("a 404 status is returned")
      response.status shouldBe NotFound

      And("the response body should contain the error message")
      response.string shouldBe EntryNotFound(testId).error

      And("the delete method should be called only once")
      (mockAlgebra.removeAirline _).verify(testId).once()
    }
  }
}
