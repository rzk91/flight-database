package flightdatabase

import org.http4s.Uri

package object testutils {
  val testId: Long = 1
  val invalid: String = "invalid"
  def createIdUri(id: String): Uri = Uri.unsafeFromString(s"/$id")
  def createIdUri(id: Long): Uri = createIdUri(id.toString)

  def createQueryUri(q: String, pathParam: Option[String] = None): Uri =
    Uri.unsafeFromString(pathParam.fold(s"?$q")(p => s"/$p?$q"))
}
