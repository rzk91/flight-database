package flightdatabase

import io.circe.generic.extras.Configuration

package object domain {
  // Allow for snake_case to camelCase conversion automatically
  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames
}
