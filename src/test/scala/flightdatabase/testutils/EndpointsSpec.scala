package flightdatabase.testutils

import cats.effect.Concurrent
import flightdatabase.api.endpoints.Endpoints
import org.http4s.EntityEncoder
import org.http4s.HttpApp
import org.http4s.Method
import org.http4s.Request
import org.http4s.Response
import org.http4s.Uri

abstract class EndpointsSpec[F[_]: Concurrent] {
  def api: Endpoints[F]
  final protected lazy val endpoints: HttpApp[F] = api.endpoints.orNotFound

  // Helper method to get responses from the endpoints
  def headResponse(uri: Uri): F[Response[F]] =
    endpoints.run(Request(method = Method.HEAD, uri = uri))

  def getResponse(uri: Uri = Uri(path = Uri.Path.Root)): F[Response[F]] =
    endpoints.run(Request(method = Method.GET, uri = uri))

  def postResponse[A](body: A, uri: Uri)(implicit enc: EntityEncoder[F, A]): F[Response[F]] =
    endpoints.run(Request(method = Method.POST, uri = uri).withEntity(body))

  def putResponse[A](body: A, uri: Uri)(implicit enc: EntityEncoder[F, A]): F[Response[F]] =
    endpoints.run(Request(method = Method.PUT, uri = uri).withEntity(body))

  def deleteResponse(uri: Uri): F[Response[F]] =
    endpoints.run(Request(method = Method.DELETE, uri = uri))
}
