package flightdatabase.utils.implicits

import cats.Monad
import cats.data.Kleisli
import cats.data.OptionT
import flightdatabase.domain.EntryInvalidFormat
import fs2.Stream
import fs2.text.utf8
import org.http4s.Charset
import org.http4s.Headers
import org.http4s.MediaType
import org.http4s.Request
import org.http4s.Response
import org.http4s.Response.notFoundFor
import org.http4s.Status
import org.http4s.headers.`Content-Length`
import org.http4s.headers.`Content-Type`

final class RichKleisliResponse[F[_]: Monad](
  self: Kleisli[OptionT[F, *], Request[F], Response[F]]
) {

  private val badRequest: Response[F] = {
    val error = EntryInvalidFormat.error
    Response(
      Status.BadRequest,
      body = Stream(error).through(utf8.encode),
      headers = Headers(
        `Content-Type`(MediaType.text.plain, Charset.`UTF-8`),
        `Content-Length`.unsafeFromLong(error.length)
      )
    )
  }

  def orBadRequest: Kleisli[F, Request[F], Response[F]] =
    Kleisli(req => self.run(req).getOrElse(badRequest))

  def orNotFoundIf(cond: Request[F] => Boolean): Kleisli[OptionT[F, *], Request[F], Response[F]] =
    Kleisli(req => OptionT.whenF(cond(req))(notFoundFor(req)).orElse(self.run(req)))
}
