package flightdatabase

import io.circe.generic.extras.Configuration

package object domain {
  // Allow for snake_case to camelCase conversion automatically
  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames

  // Define the type alias for the API result
  type ApiResult[O] = Either[ApiError, ApiOutput[O]]

  // Lift to API Result
  def toApiResult[A](value: A): ApiResult[A] = Got[A](value).asResult

  def listToApiResult[A](list: List[A]): ApiResult[List[A]] =
    if (list.isEmpty) EntryListEmpty.asResult else toApiResult(list)
}
