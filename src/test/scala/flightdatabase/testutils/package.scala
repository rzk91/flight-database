package flightdatabase

import org.http4s.Uri

package object testutils {
  val testId: Long = 1
  val invalidTestId: String = "bla"
  def createUri(id: String): Uri = Uri.unsafeFromString(s"/$id")
  def createUri(id: Long): Uri = createUri(id.toString)

  def createUri(q: String, pathParam: Option[String]): Uri =
    Uri.unsafeFromString(pathParam.fold(s"?$q")(p => s"/$p?$q"))
}
