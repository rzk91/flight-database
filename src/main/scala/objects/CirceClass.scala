package objects

import io.circe.generic.extras.Configuration

trait CirceClass {
  def sqlInsert: String

  def insertWithNull(field: Option[_]): String =
    field.map(v => s"'$v'").orNull

  def selectIdStmt(
    table: String,
    key: Option[String],
    keyField: String = "name"
  ): String =
    key.map(k => s"SELECT id FROM $table WHERE $keyField = \'$k\'").orNull
}

object CirceClass {

  // Allow for snake_case to camelCase conversion automatically
  implicit val config: Configuration =
    Configuration.default.withSnakeCaseMemberNames
}
