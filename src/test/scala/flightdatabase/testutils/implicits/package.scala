package flightdatabase.testutils

import cats.effect.IO
import org.http4s.Response

package object implicits {

  @inline implicit def enrichResponseIO(response: Response[IO]): RichResponseIO =
    new RichResponseIO(response)
}
