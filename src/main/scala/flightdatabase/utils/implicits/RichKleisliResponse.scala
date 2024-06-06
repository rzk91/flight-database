package flightdatabase.utils.implicits

import cats.Monad
import cats.data.Kleisli
import cats.data.OptionT
import flightdatabase.domain.EntryInvalidFormat
import fs2.Pure
import fs2.Stream
import fs2.text.utf8
import org.http4s.Charset
import org.http4s.Headers
import org.http4s.MediaType
import org.http4s.Response
import org.http4s.Response.notFound
import org.http4s.Status
import org.http4s.headers.`Content-Length`
import org.http4s.headers.`Content-Type`

class RichKleisliResponse[F[_]: Monad, A](self: Kleisli[OptionT[F, *], A, Response[F]]) {

  private val pureBadRequest: Response[Pure] = {
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

  private def badRequest: Response[F] = pureBadRequest.covary[F]

  def orBadRequest: Kleisli[F, A, Response[F]] =
    Kleisli(a => self.run(a).getOrElse(badRequest))

  def orNotFoundIf(cond: A => Boolean): Kleisli[OptionT[F, *], A, Response[F]] =
    Kleisli(a => OptionT.when(cond(a))(notFound[F]).orElse(self.run(a)))
}
