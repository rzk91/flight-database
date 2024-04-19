package flightdatabase.repository.queries

import doobie.Fragment
import doobie.Put
import doobie.Query0
import doobie.implicits._
import doobie.util.update.Update0
import flightdatabase.domain.language.Language
import flightdatabase.domain.language.LanguageCreate

private[repository] object LanguageQueries {

  def languageExists(id: Long): Query0[Boolean] = idExistsQuery[Language](id)

  def selectAllLanguages: Query0[Language] = selectAll.query[Language]

  def selectLanguageBy[V: Put](field: String, value: V): Query0[Language] =
    (selectAll ++ whereFragment(s"language.$field", value)).query[Language]

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
