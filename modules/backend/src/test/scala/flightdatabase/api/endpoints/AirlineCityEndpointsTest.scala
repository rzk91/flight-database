package flightdatabase.api.endpoints

import cats.data.{NonEmptyList => Nel}
import cats.effect.IO
import flightdatabase.ApiResult
import flightdatabase._
import flightdatabase.airline_city.AirlineCity
import flightdatabase.airline_city.AirlineCityAlgebra
import flightdatabase.airline_city.AirlineCityCreate
import flightdatabase.airline_city.AirlineCityPatch
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy
import flightdatabase.testutils.endpoints.CompositeIdBehavior
import flightdatabase.testutils.endpoints.EntityEndpointsSpec
import flightdatabase.testutils.endpoints.FieldFixture
import io.circe.Decoder
import io.circe.Encoder
import org.scalamock.function.StubFunction1
import org.scalamock.function.StubFunction2

final class AirlineCityEndpointsTest
    extends EntityEndpointsSpec[AirlineCity, AirlineCityCreate, AirlineCityPatch]
    with CompositeIdBehavior[AirlineCity, AirlineCityCreate, AirlineCityPatch] {

  val mockAlgebra: AirlineCityAlgebra[IO] = stub[AirlineCityAlgebra[IO]]
  override val api: Endpoints[IO] = AirlineCityEndpoints[IO]("/airline-cities", mockAlgebra)

  override val mockGetAll: PartiallyAppliedGetAll[IO,AirlineCity] = stub[PartiallyAppliedGetAll[IO, AirlineCity]]
  override val mockGetBy: PartiallyAppliedGetBy[IO,AirlineCity] = stub[PartiallyAppliedGetBy[IO, AirlineCity]]

  val table: TableBase[AirlineCity] = AirlineCity.airlineCityTableBase
  val modelDecoder: Decoder[AirlineCity] = Decoder[AirlineCity]
  val modelEncoder: Encoder[AirlineCity] = Encoder[AirlineCity]
  val createEncoder: Encoder[AirlineCityCreate] = Encoder[AirlineCityCreate]
  val patchEncoder: Encoder[AirlineCityPatch] = Encoder[AirlineCityPatch]

  // Mirrors `originalAirlineCities` in AirlineCityRepositoryIT.
  val samples: Nel[AirlineCity] = Nel.of(
    AirlineCity(1, 1, 2),
    AirlineCity(2, 2, 4)
  )

  // Own fields are all Long.
  val fieldFixtures: List[FieldFixture[_]] = List(
    FieldFixture("airline_id", 1L, Operator.Equals, LongType)
  )

  private val airlineFixtures: List[FieldFixture[_]] = List(
    FieldFixture("name", "Lufthansa", Operator.Equals, StringType),
    FieldFixture("id", 1L, Operator.In, LongType)
  )

  private val cityFixtures: List[FieldFixture[_]] = List(
    FieldFixture("name", "Dubai", Operator.Equals, StringType),
    FieldFixture("population", 1000000L, Operator.GreaterThan, LongType)
  )

  def existsStub: StubFunction1[Long,IO[Boolean]] = mockAlgebra.doesAirlineCityExist _
  def getByIdStub: StubFunction1[Long,IO[ApiResult[AirlineCity]]] = mockAlgebra.getAirlineCity(_: Long)
  def compositeStub: StubFunction2[Long,Long,IO[ApiResult[AirlineCity]]] = mockAlgebra.getAirlineCity(_: Long, _: Long)
  def compositePath(leftId: String, rightId: String): String = s"airline/$leftId/city/$rightId"
  def createStub: StubFunction1[AirlineCityCreate,IO[ApiResult[Long]]] = mockAlgebra.createAirlineCity _
  def updateStub: StubFunction1[AirlineCity,IO[ApiResult[Long]]] = mockAlgebra.updateAirlineCity _
  def patchStub: StubFunction2[Long,AirlineCityPatch,IO[ApiResult[AirlineCity]]] = mockAlgebra.partiallyUpdateAirlineCity _
  def removeStub: StubFunction1[Long,IO[ApiResult[Unit]]] = mockAlgebra.removeAirlineCity _

  def armGetAll(): Unit = (() => mockAlgebra.getAirlineCities).when().returns(mockGetAll)
  def armGetBy(): Unit = (() => mockAlgebra.getAirlineCitiesBy).when().returns(mockGetBy)

  private def armByAirline(): Unit =
    (() => mockAlgebra.getAirlineCitiesByAirline).when().returns(mockGetBy)

  private def armByCity(): Unit =
    (() => mockAlgebra.getAirlineCitiesByCity).when().returns(mockGetBy)

  val sampleCreate: AirlineCityCreate = AirlineCityCreate(1, 3)

  def fromCreate(id: Long, create: AirlineCityCreate): AirlineCity =
    AirlineCity.fromCreate(id, create)

  def withCreateId(create: AirlineCityCreate, id: Long): AirlineCityCreate =
    create.copy(id = Some(id))
  val samplePatch: AirlineCityPatch = AirlineCityPatch(cityId = Some(3))

  testExistenceBehavior()
  testGetByIdBehavior()
  testCompositeIdBehavior()
  testGetAllBehavior()
  testGetByFieldBehavior()
  testExternalFilterBehavior("airline", () => armByAirline(), airlineFixtures)
  testExternalFilterBehavior("city", () => armByCity(), cityFixtures)
  testCrudBehavior()
}
