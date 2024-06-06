package flightdatabase.testutils

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.api.endpoints.Endpoints
import flightdatabase.utils.implicits.enrichKleisliResponse
import org.http4s.EntityEncoder
import org.http4s.HttpApp
import org.http4s.Method
import org.http4s.Request
import org.http4s.Response
import org.http4s.Uri

abstract class IOEndpointsSpec {
  def api: Endpoints[IO]
  final protected lazy val endpoints: HttpApp[IO] = api.endpoints.orBadRequest

  // Helper method to get responses from the endpoints
  def headResponse(uri: Uri): Response[IO] =
    endpoints.run(Request(method = Method.HEAD, uri = uri)).unsafeRunSync()

  def getResponse(uri: Uri = rootUri): Response[IO] =
    endpoints.run(Request(method = Method.GET, uri = uri)).unsafeRunSync()

  def postResponse[A](body: A, uri: Uri = rootUri)(
    implicit enc: EntityEncoder[IO, A]
  ): Response[IO] =
    endpoints.run(Request(method = Method.POST, uri = uri).withEntity(body)).unsafeRunSync()

  def putResponse[A](body: A, uri: Uri)(implicit enc: EntityEncoder[IO, A]): Response[IO] =
    endpoints.run(Request(method = Method.PUT, uri = uri).withEntity(body)).unsafeRunSync()

  def patchResponse[A](body: A, uri: Uri)(implicit enc: EntityEncoder[IO, A]): Response[IO] =
    endpoints.run(Request(method = Method.PATCH, uri = uri).withEntity(body)).unsafeRunSync()

  def deleteResponse(uri: Uri): Response[IO] =
    endpoints.run(Request(method = Method.DELETE, uri = uri)).unsafeRunSync()
}
