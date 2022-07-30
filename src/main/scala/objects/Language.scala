import io.circe.generic.extras._

@ConfiguredJsonCodec final case class Language(
  name: String,
  iso2: String,
  iso3: String,
  originalName: String
) {

  def sqlInsert: String =
    s"INSERT INTO language (name, iso2, iso3, original_name) " +
    s"VALUES ('$name', '$iso2', '$iso3', '$originalName');"
}

object Language {

  implicit val config: Configuration =
    Configuration.default.withSnakeCaseMemberNames
}
