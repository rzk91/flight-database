package flightdatabase.api.endpoints

import cats.effect.Sync
import org.http4s._
import org.http4s.headers.{`Content-Type` => ContentTypeHeader}

import scala.io.Source
import scala.util.Using

// Serves the OpenAPI spec plus a Scalar (github.com/scalar/scalar) reference UI under this
// endpoint's prefix, sourced entirely from the classpath (webjar dependency, no CDN) so it
// works fully offline once built.
class ApiDocsEndpoints[F[_]: Sync] private (prefix: String, webjarVersion: String)
    extends Endpoints[F](prefix) {

  private val webjarBase = s"META-INF/resources/webjars/scalar__api-reference/$webjarVersion"

  private lazy val indexHtml: String =
    Using.resource(Source.fromResource("docs/index.html"))(_.mkString)

  override val endpoints: HttpRoutes[F] = HttpRoutes.of {
    // GET /docs (and /docs/) -> the Scalar shell, with request-derived absolute asset URLs
    // so they resolve correctly regardless of the trailing slash or where the app is mounted.
    case req @ GET -> Root =>
      val base = req.uri.path.dropEndsWithSlash.renderString
      Ok(indexHtml.replace("{{base}}", base), ContentTypeHeader(MediaType.text.html))

    // GET /docs/openapi.yaml -> the hand-maintained spec, already a classpath resource.
    case req @ GET -> Root / "openapi.yaml" =>
      StaticFile.fromResource[F]("openapi.yaml", Some(req)).getOrElseF(NotFound())

    // GET /docs/{asset...} -> the pinned Scalar webjar's static assets, e.g.
    // dist/browser/standalone.js. Must stay last: it matches any remaining path.
    case req @ GET -> path =>
      val asset = path.segments.map(_.decoded()).mkString("/")
      StaticFile.fromResource[F](s"$webjarBase/$asset", Some(req)).getOrElseF(NotFound())
  }
}

object ApiDocsEndpoints {

  // Keep in sync with `scalarApiReferenceVersion` in build.sbt.
  val WebjarVersion: String = "1.47.0"

  def apply[F[_]: Sync](prefix: String, webjarVersion: String = WebjarVersion): Endpoints[F] =
    new ApiDocsEndpoints[F](prefix, webjarVersion)
}
