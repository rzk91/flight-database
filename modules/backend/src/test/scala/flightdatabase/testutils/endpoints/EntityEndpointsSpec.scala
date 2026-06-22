package flightdatabase.testutils.endpoints

import cats.data.{NonEmptyList => Nel}
import cats.effect.IO
import doobie.Put
import doobie.Read
import flightdatabase._
import flightdatabase.extensions.test._
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy
import flightdatabase.testutils._
import io.circe.Decoder
import io.circe.Encoder
import org.http4s.circe.CirceEntityCodec._
import org.http4s.Status.{Created => CreatedStatus, _}
import org.scalamock.function.StubFunction1
import org.scalamock.function.StubFunction2
import org.scalamock.function.StubFunction3
import org.scalamock.function.StubFunction5
import org.scalamock.scalatest.MockFactory
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpecLike
import org.scalatest.matchers.should.Matchers

/**
  * Shared base for endpoint unit tests. It owns every scenario whose behaviour is identical across
  * entities (the structural CRUD/HEAD/GET shapes and their error paths) and exposes them as
  * `*Behavior(...)` registration methods in the spirit of ScalaTest's "shared tests".
  *
  * A concrete spec mixes this in, supplies the abstract wiring (sample data, the algebra stub
  * handles, the field fixtures) as `val`s/`def`s, and then CALLS the behavior methods it wants from
  * its body. Calling (rather than auto-registering in a trait body) guarantees the abstract members
  * are initialised before any scenario is registered.
  *
  * Type parameters: `Model` is the entity/table type, `Create` its create payload, `Patch` its
  * patch payload.
  *
  * What stays per entity: sample rows, field fixtures, the stub handles, and the create/patch
  * construction hooks. Everything else lives here once.
  */
abstract class EntityEndpointsSpec[Model, Create, Patch]
    extends IOEndpointsSpec
    with AnyFeatureSpecLike
    with GivenWhenThen
    with Matchers
    with CustomMatchers
    with MockFactory {

  // ---------------------------------------------------------------------------
  // Per-entity wiring (abstract)
  // ---------------------------------------------------------------------------

  /** TableBase drives the qualified field name (`<table>.<field>`) the endpoints build. */
  implicit def table: TableBase[Model]

  implicit def modelDecoder: Decoder[Model]
  implicit def modelEncoder: Encoder[Model]
  implicit def createEncoder: Encoder[Create]
  implicit def patchEncoder: Encoder[Patch]

  /** Representative rows. Returned verbatim by stubbed reads, so ordering need not be realistic. */
  def samples: Nel[Model]

  /** One fixture per field type the entity exposes for its own `/filter` and `return-only`. */
  def fieldFixtures: List[FieldFixture[_]]

  // Stub handles for the arg-ful algebra methods (eta-expanded => scalamock StubFunctions).
  def existsStub: StubFunction1[Long, IO[Boolean]]
  def getByIdStub: StubFunction1[Long, IO[ApiResult[Model]]]
  def createStub: StubFunction1[Create, IO[ApiResult[Long]]]
  def updateStub: StubFunction1[Model, IO[ApiResult[Long]]]
  def patchStub: StubFunction2[Long, Patch, IO[ApiResult[Model]]]
  def removeStub: StubFunction1[Long, IO[ApiResult[Unit]]]

  // The partially applied getters are no-arg methods; scalamock wants them armed via `(() => ...)`,
  // so each entity supplies a one-line arming function plus the stub instance it returns.
  val mockGetAll: PartiallyAppliedGetAll[IO, Model]
  val mockGetBy: PartiallyAppliedGetBy[IO, Model]
  def armGetAll(): Unit
  def armGetBy(): Unit

  // CRUD construction hooks (no generic `toCreate` exists on the models).
  def sampleCreate: Create
  def fromCreate(id: Long, create: Create): Model
  def withCreateId(create: Create, id: Long): Create
  def samplePatch: Patch

  // ---------------------------------------------------------------------------
  // Derived helpers
  // ---------------------------------------------------------------------------

  private def tableName: String = table.asString
  private def qualified(field: String): String = s"$tableName.$field"

  // protected (not private) so the opt-in mixins (e.g. direction-flag filters) can reuse them.
  protected val emptySortAndLimit: ValidatedSortAndLimit = ValidatedSortAndLimit.empty

  private def mockAll: StubFunction1[ValidatedSortAndLimit, IO[ApiResult[Nel[Model]]]] =
    mockGetAll.apply(_: ValidatedSortAndLimit)

  private def mockAllOnly[V]
    : StubFunction3[ValidatedSortAndLimit, String, Read[V], IO[ApiResult[Nel[V]]]] =
    mockGetAll.apply(_: ValidatedSortAndLimit, _: String)(_: Read[V])

  protected def mockBy[V]: StubFunction5[
    String,
    Nel[V],
    Operator,
    ValidatedSortAndLimit,
    Put[V],
    IO[ApiResult[Nel[Model]]]
  ] = mockGetBy.apply(_: String, _: Nel[V], _: Operator, _: ValidatedSortAndLimit)(_: Put[V])

  /** An operator guaranteed NOT to be valid for the given field type (for WrongOperator tests). */
  private def wrongOperatorFor(fieldType: FieldType): Operator =
    fieldType match {
      case StringType | BooleanType => Operator.GreaterThan
      case _                        => Operator.StartsWith
    }

  // ---------------------------------------------------------------------------
  // Behaviors
  // ---------------------------------------------------------------------------

  protected def testExistenceBehavior(): Unit =
    Feature(s"Checking if a $tableName exists") {
      Scenario(s"An existing $tableName") {
        Given("an existing ID")
        existsStub.when(testId).returns(IO.pure(true))

        When("the entity is checked")
        val response = headResponse(createIdUri(testId))

        Then("a 200 status is returned")
        response.status shouldBe Ok

        And("the method is called once")
        existsStub.verify(testId).once()
      }

      Scenario(s"A non-existing $tableName") {
        Given("a non-existing ID")
        existsStub.when(testId).returns(IO.pure(false))

        When("the entity is checked")
        val response = headResponse(createIdUri(testId))

        Then("a 404 status is returned")
        response.status shouldBe NotFound

        And("the method is called once")
        existsStub.verify(testId).once()
      }

      Scenario(s"An invalid $tableName ID for existence") {
        Given("an invalid ID")

        When("the entity is checked")
        val response = headResponse(createIdUri(invalid))

        Then("a 400 status is returned")
        response.status shouldBe BadRequest

        And("the right error message is returned")
        response.string shouldBe EntryInvalidFormat.error

        And("the method is never called")
        existsStub.verify(*).never()
      }
    }

  protected def testGetByIdBehavior(): Unit =
    Feature(s"Fetching a $tableName by ID") {
      Scenario(s"Fetching an existing $tableName") {
        Given("an existing ID")
        getByIdStub.when(testId).returns(Got(samples.head).elevate[IO])

        When("the entity is fetched")
        val response = getResponse(createIdUri(testId))

        Then("a 200 status is returned")
        response.status shouldBe Ok

        And("the body contains the entity")
        response.extract[Model] shouldBe samples.head

        And("the method is called once")
        getByIdStub.verify(testId).once()
      }

      Scenario(s"Fetching a non-existing $tableName") {
        val notFound = EntryNotFound(testId)
        Given("a non-existing ID")
        getByIdStub.when(testId).returns(notFound.elevate[IO, Model])

        When("the entity is fetched")
        val response = getResponse(createIdUri(testId))

        Then("a 404 status is returned")
        response.status shouldBe NotFound

        And("the body contains the error")
        response.string shouldBe notFound.error

        And("the method is called once")
        getByIdStub.verify(testId).once()
      }

      Scenario(s"Fetching a $tableName with an invalid ID") {
        Given("an invalid ID")

        When("the entity is fetched")
        val response = getResponse(createIdUri(invalid))

        Then("a 400 status is returned")
        response.status shouldBe BadRequest

        And("the right error message is returned")
        response.string shouldBe EntryInvalidFormat.error

        And("the method is never called")
        getByIdStub.verify(*).never()
      }
    }

  protected def testGetAllBehavior(): Unit =
    Feature(s"Fetching all ${tableName}s") {
      Scenario(s"Fetching all ${tableName}s") {
        armGetAll()
        Given("no query parameters")
        mockAll.when(emptySortAndLimit).returns(Got(samples).elevate[IO])

        When("all entities are fetched")
        val response = getResponse()

        Then("a 200 status is returned")
        response.status shouldBe Ok

        And("the body contains all entities")
        response.extract[Nel[Model]] shouldBe samples

        And("the all-getter is called once and the only-getter never")
        mockAll.verify(emptySortAndLimit).once()
        mockAllOnly[String].verify(*, *, *).never()
      }

      Scenario("An empty list is returned") {
        armGetAll()
        Given("no entities in the database")
        mockAll.when(emptySortAndLimit).returns(EntryListEmpty.elevate[IO, Nel[Model]])

        When("all entities are fetched")
        val response = getResponse()

        Then("a 200 status is returned")
        response.status shouldBe Ok

        And("the body indicates an empty list")
        response.string shouldBe EntryListEmpty.error
      }

      // One return-only scenario per declared field type.
      fieldFixtures.foreach(fx => returnOnlyScenario(fx))

      Scenario("Sorting, limiting, and offsetting all entities") {
        val sortField = fieldFixtures.head.field
        val sal = ValidatedSortAndLimit(
          sortBy = Some(qualified(sortField)),
          order = Some(ResultOrder.Descending),
          limit = Some(1),
          offset = Some(1)
        )
        armGetAll()
        Given("sort, order, limit, and offset parameters")
        mockAll.when(sal).returns(Got(samples).elevate[IO])

        When("all entities are fetched")
        val query = s"sort-by=$sortField&order=desc&limit=1&offset=1"
        val response = getResponse(createQueryUri(query))

        Then("a 200 status is returned")
        response.status shouldBe Ok

        And("the all-getter is called once with the parsed sort/limit")
        mockAll.verify(sal).once()
      }

      Scenario("An invalid return-only field is passed") {
        Given("an invalid return-only field")
        val query = s"return-only=$invalid"

        When("all entities are fetched")
        val response = getResponse(createQueryUri(query))

        Then("a 400 status is returned")
        response.status shouldBe BadRequest

        And("the body contains the error")
        response.string shouldBe InvalidField(invalid).error

        And("no getter is called")
        mockAll.verify(*).never()
        mockAllOnly[String].verify(*, *, *).never()
      }

      Scenario("Invalid sort or limit parameters are passed") {
        Given("invalid sort and limit parameters")
        val query = s"sort-by=$invalid&limit=-1"

        When("all entities are fetched")
        val response = getResponse(createQueryUri(query))

        Then("a 400 status is returned")
        response.status shouldBe BadRequest

        And("all offending parameters are named in the body")
        response.string should includeAllOf(query.split("[&=]").toIndexedSeq: _*)

        And("no getter is called")
        mockAll.verify(*).never()
      }
    }

  private def returnOnlyScenario[V](fx: FieldFixture[V]): Unit =
    Scenario(s"Fetching only the ${fx.field} field (${fx.fieldType.asString})") {
      implicit val dec: Decoder[V] = fx.decoder
      val only = Nel.one(fx.value)
      armGetAll()
      Given(s"a return-only query for ${fx.field}")
      mockAllOnly[V].when(emptySortAndLimit, qualified(fx.field), *).returns(Got(only).elevate[IO])

      When("the field is fetched")
      val response = getResponse(createQueryUri(s"return-only=${fx.field}"))

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the body contains only that field")
      response.extract[Nel[V]] shouldBe only

      And("the only-getter is called once and the all-getter never")
      mockAllOnly[V].verify(emptySortAndLimit, qualified(fx.field), *).once()
      mockAll.verify(*).never()
    }

  /** Own-table `/filter`, driven by the entity's own [[fieldFixtures]]. */
  protected def testGetByFieldBehavior(): Unit =
    filterBehavior(
      s"Fetching ${tableName}s by an own field",
      Some("filter"),
      () => armGetBy(),
      fieldFixtures
    )

  /**
    * External-table `/<segment>/filter`. The `fixtures` describe the EXTERNAL table's fields, and
    * `armStub` must point that external accessor at the shared `mockGetBy` stub.
    */
  protected def testExternalFilterBehavior(
    segment: String,
    armStub: () => Unit,
    fixtures: List[FieldFixture[_]]
  ): Unit =
    filterBehavior(
      s"Fetching ${tableName}s by a $segment field",
      Some(s"$segment/filter"),
      armStub,
      fixtures
    )

  protected def filterBehavior(
    featureName: String,
    path: Option[String],
    armStub: () => Unit,
    fixtures: List[FieldFixture[_]]
  ): Unit =
    Feature(featureName) {
      fixtures.foreach(fx => filterScenario(path, armStub, fx))
      fixtures.headOption.foreach(fx => defaultOperatorScenario(path, armStub, fx))

      Scenario(s"$featureName: invalid field") {
        Given("an invalid field")
        val query = s"field=$invalid&value=1"

        When("entities are fetched")
        val response = getResponse(createQueryUri(query, path))

        Then("a 400 status is returned")
        response.status shouldBe BadRequest
        response.string shouldBe InvalidField(invalid).error
      }

      Scenario(s"$featureName: empty field") {
        Given("an empty field")
        val query = "field=&value=1"

        When("entities are fetched")
        val response = getResponse(createQueryUri(query, path))

        Then("a 400 status is returned")
        response.status shouldBe BadRequest
        response.string shouldBe InvalidField("").error
      }

      Scenario(s"$featureName: invalid operator name") {
        Given("an unparseable operator")
        val field = fixtures.head.field
        val query = s"field=$field&operator=$invalid&value=1"

        When("entities are fetched")
        val response = getResponse(createQueryUri(query, path))

        Then("a 400 status is returned")
        response.status shouldBe BadRequest
        response.string should include(invalid)
      }

      Scenario(s"$featureName: operator invalid for the field type") {
        val fx = fixtures.head
        val wrongOp = wrongOperatorFor(fx.fieldType)
        Given("an operator that does not apply to the field's type")
        val query = s"field=${fx.field}&operator=${wrongOp.entryName}&value=1"

        When("entities are fetched")
        val response = getResponse(createQueryUri(query, path))

        Then("a 400 status is returned")
        response.status shouldBe BadRequest
        response.string shouldBe WrongOperator(wrongOp, fx.field, fx.fieldType).error
      }

      Scenario(s"$featureName: invalid filter path") {
        Given("a filter path that matches no route")
        val badPath = path.map(_.stripSuffix("filter") + invalid)
        val query = "field=id&value=1"

        When("entities are fetched")
        val response = getResponse(createQueryUri(query, badPath))

        Then("a 400 status is returned")
        response.status shouldBe BadRequest
        response.string shouldBe EntryInvalidFormat.error
      }

      Scenario(s"$featureName: no query parameters") {
        Given("no query parameters")

        When("entities are fetched")
        val response = getResponse(createQueryUri("", path))

        Then("a 400 status is returned")
        response.status shouldBe BadRequest
        response.string shouldBe EntryInvalidFormat.error
      }
    }

  private def filterScenario[V](
    path: Option[String],
    armStub: () => Unit,
    fx: FieldFixture[V]
  ): Unit =
    Scenario(s"Filtering by ${fx.field} (${fx.fieldType.asString})") {
      implicit val put: Put[V] = fx.put
      armStub()
      Given(s"a value for ${fx.field}")
      mockBy[V]
        .when(fx.field, Nel.one(fx.value), fx.operator, emptySortAndLimit, *)
        .returns(Got(samples).elevate[IO])

      When("entities are fetched")
      val query = s"field=${fx.field}&value=${fx.valueString}&operator=${fx.operator.entryName}"
      val response = getResponse(createQueryUri(query, path))

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the body contains the entities")
      response.extract[Nel[Model]] shouldBe samples

      And("the getter is called once with the parsed arguments")
      mockBy[V].verify(fx.field, Nel.one(fx.value), fx.operator, emptySortAndLimit, *).once()
    }

  /** An omitted `operator` parameter must default to `eq` (the endpoint matcher's default). */
  private def defaultOperatorScenario[V](
    path: Option[String],
    armStub: () => Unit,
    fx: FieldFixture[V]
  ): Unit =
    Scenario(s"Filtering by ${fx.field} with the operator omitted defaults to eq") {
      implicit val put: Put[V] = fx.put
      armStub()
      Given("a filter with no operator parameter")
      mockBy[V]
        .when(fx.field, Nel.one(fx.value), Operator.Equals, emptySortAndLimit, *)
        .returns(Got(samples).elevate[IO])

      When("entities are fetched without an operator")
      val query = s"field=${fx.field}&value=${fx.valueString}"
      val response = getResponse(createQueryUri(query, path))

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the getter is called with the default Equals operator")
      mockBy[V].verify(fx.field, Nel.one(fx.value), Operator.Equals, emptySortAndLimit, *).once()
    }

  protected def testCrudBehavior(): Unit = {
    Feature(s"Creating a $tableName") {
      Scenario(s"A valid $tableName is created") {
        Given("a valid create body")
        val create = sampleCreate
        createStub.when(create).returns(Created(testId).elevate[IO])

        When("the entity is created")
        val response = postResponse(create)

        Then("a 201 status is returned")
        response.status shouldBe CreatedStatus

        And("the body contains the new ID")
        response.extract[Long] shouldBe testId

        And("the create method is called once")
        createStub.verify(create).once()
      }

      Scenario(s"An invalid $tableName is created") {
        Given("an invalid body")
        val response = postResponse(InvalidFlightDbObject.instance)

        Then("a 400 status is returned")
        response.status shouldBe BadRequest
        response.string shouldBe EntryInvalidFormat.error

        And("the create method is never called")
        createStub.verify(*).never()
      }

      Scenario(s"An already existing $tableName is created") {
        Given("a create body for an existing entity")
        val create = sampleCreate
        createStub.when(create).returns(EntryAlreadyExists.elevate[IO, Long])

        When("the entity is created")
        val response = postResponse(create)

        Then("a 409 status is returned")
        response.status shouldBe Conflict
        response.string shouldBe EntryAlreadyExists.error

        And("the create method is called once")
        createStub.verify(create).once()
      }
    }

    Feature(s"Updating a $tableName") {
      Scenario(s"A valid $tableName is updated") {
        val create = sampleCreate
        val entity = fromCreate(testId, create)
        Given("an existing ID and a valid body")
        updateStub.when(entity).returns(Updated(testId).elevate[IO])

        When("the entity is updated")
        val response = putResponse(create, createIdUri(testId))

        Then("a 200 status is returned")
        response.status shouldBe Ok

        And("the body contains the updated ID")
        response.extract[Long] shouldBe testId

        And("the update method is called once")
        updateStub.verify(entity).once()
      }

      Scenario(s"An invalid $tableName update body is passed") {
        Given("an invalid body")
        val response = putResponse(InvalidFlightDbObject.instance, createIdUri(testId))

        Then("a 400 status is returned")
        response.status shouldBe BadRequest
        response.string shouldBe EntryInvalidFormat.error

        And("the update method is never called")
        updateStub.verify(*).never()
      }

      Scenario(s"An invalid ID is passed on update") {
        Given("an invalid ID")
        val response = putResponse(sampleCreate, createIdUri(invalid))

        Then("a 400 status is returned")
        response.status shouldBe BadRequest
        response.string shouldBe EntryInvalidFormat.error

        And("the update method is never called")
        updateStub.verify(*).never()
      }

      Scenario(s"Inconsistent IDs are passed on update") {
        Given("a body whose ID differs from the URL")
        val mismatchId = testId + 1
        val body = withCreateId(sampleCreate, mismatchId)

        When("the entity is updated")
        val response = putResponse(body, createIdUri(testId))

        Then("a 400 status is returned")
        response.status shouldBe BadRequest
        response.string shouldBe InconsistentIds(testId, mismatchId).error

        And("the update method is never called")
        updateStub.verify(*).never()
      }

      // Symmetric with the patch route: confirm the update route forwards an algebra error.
      Scenario("An algebra error on update is forwarded") {
        val create = sampleCreate
        val entity = fromCreate(testId, create)
        Given("an existing ID and a body the algebra rejects")
        updateStub.when(entity).returns(EntryHasInvalidForeignKey.elevate[IO, Long])

        When("the entity is updated")
        val response = putResponse(create, createIdUri(testId))

        Then("a 400 status is returned")
        response.status shouldBe BadRequest

        And("the body contains the algebra's error")
        response.string shouldBe EntryHasInvalidForeignKey.error

        And("the update method is called once")
        updateStub.verify(entity).once()
      }
    }

    Feature(s"Partially updating a $tableName") {
      Scenario("A valid patch is passed") {
        val patch = samplePatch
        Given("an existing ID and a valid patch")
        patchStub.when(testId, patch).returns(Updated(samples.head).elevate[IO])

        When("the entity is patched")
        val response = patchResponse(patch, createIdUri(testId))

        Then("a 200 status is returned")
        response.status shouldBe Ok

        And("the body contains the updated entity")
        response.extract[Model] shouldBe samples.head

        And("the patch method is called once")
        patchStub.verify(testId, patch).once()
      }

      Scenario("An invalid patch body is passed") {
        Given("an invalid patch body")
        val response = patchResponse(invalid, createIdUri(testId))

        Then("a 400 status is returned")
        response.status shouldBe BadRequest
        response.string shouldBe EntryInvalidFormat.error

        And("the patch method is never called")
        patchStub.verify(*, *).never()
      }

      Scenario("An invalid ID is passed on patch") {
        Given("an invalid ID")
        val response = patchResponse(samplePatch, createIdUri(invalid))

        Then("a 400 status is returned")
        response.status shouldBe BadRequest
        response.string shouldBe EntryInvalidFormat.error

        And("the patch method is never called")
        patchStub.verify(*, *).never()
      }

      // Representative check that the route forwards an algebra error to the response mapping.
      // The full ApiError -> status table is shared and exercised once, not per entity.
      Scenario("An algebra error on patch is forwarded") {
        Given("a patch the algebra rejects")
        patchStub.when(testId, samplePatch).returns(EntryCheckFailed.elevate[IO, Model])

        When("the entity is patched")
        val response = patchResponse(samplePatch, createIdUri(testId))

        Then("a 400 status is returned")
        response.status shouldBe BadRequest

        And("the body contains the algebra's error")
        response.string shouldBe EntryCheckFailed.error

        And("the patch method is called once")
        patchStub.verify(testId, samplePatch).once()
      }
    }

    Feature(s"Deleting a $tableName") {
      Scenario(s"An existing $tableName is deleted") {
        Given("an existing ID")
        removeStub.when(testId).returns(Deleted.elevate[IO])

        When("the entity is deleted")
        val response = deleteResponse(createIdUri(testId))

        Then("a 204 status is returned")
        response.status shouldBe NoContent

        And("the body is empty")
        response.string shouldBe empty

        And("the delete method is called once")
        removeStub.verify(testId).once()
      }

      Scenario(s"An invalid ID is passed on delete") {
        Given("an invalid ID")
        val response = deleteResponse(createIdUri(invalid))

        Then("a 400 status is returned")
        response.status shouldBe BadRequest
        response.string shouldBe EntryInvalidFormat.error

        And("the delete method is never called")
        removeStub.verify(*).never()
      }

      Scenario(s"A non-existing $tableName is deleted") {
        Given("a non-existing ID")
        removeStub.when(testId).returns(EntryNotFound(testId).elevate[IO, Unit])

        When("the entity is deleted")
        val response = deleteResponse(createIdUri(testId))

        Then("a 404 status is returned")
        response.status shouldBe NotFound
        response.string shouldBe EntryNotFound(testId).error

        And("the delete method is called once")
        removeStub.verify(testId).once()
      }
    }
  }
}
