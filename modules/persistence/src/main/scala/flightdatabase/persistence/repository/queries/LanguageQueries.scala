package flightdatabase.persistence.repository.queries

import cats.data.{NonEmptyList => Nel}
import flightdatabase.Operator
import flightdatabase.ValidatedSortAndLimit
import flightdatabase.language.Language
import flightdatabase.language.LanguageCreate
import flightdatabase.persistence.syntax.sortandlimit._
import org.typelevel.doobie.Fragment
import org.typelevel.doobie.Put
import org.typelevel.doobie.Query0
import org.typelevel.doobie.implicits._
import org.typelevel.doobie.util.update.Update0

private[repository] object LanguageQueries {

  def languageExists(id: Long): Query0[Boolean] = idExistsQuery[Language](id)

  def selectAllLanguages(sortAndLimit: ValidatedSortAndLimit): Query0[Language] =
    (selectAll ++ sortAndLimit.fragment).query[Language]

  def selectLanguageBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator,
    sortAndLimit: ValidatedSortAndLimit
  ): Query0[Language] =
    (selectAll ++ whereFragment(s"language.$field", values, operator) ++ sortAndLimit.fragment)
      .query[Language]

  def insertLanguage(model: LanguageCreate): Update0 =
    sql"""
       | INSERT INTO language (name, iso2, iso3, original_name) 
       | VALUES (${model.name}, ${model.iso2}, ${model.iso3}, ${model.originalName}) 
       """.stripMargin.update

  def modifyLanguage(model: Language): Update0 =
    sql"""
           | UPDATE language
           | SET
           |  name = ${model.name},
           |  iso2 = ${model.iso2},
           |  iso3 = ${model.iso3},
           |  original_name = ${model.originalName}
           | WHERE id = ${model.id}
           """.stripMargin.update

  def deleteLanguage(id: Long): Update0 = deleteWhereId[Language](id)

  private def selectAll: Fragment =
    fr"""
       | SELECT
       |  language.id, language.name, language.iso2, language.iso3, language.original_name
       | FROM language
       """.stripMargin
}
