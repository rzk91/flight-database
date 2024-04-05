package flightdatabase.repository.queries

import doobie.Query0
import doobie.implicits._
import doobie.util.update.Update0
import flightdatabase.domain.language.LanguageModel

private[repository] object LanguageQueries {

  def selectAllLanguages: Query0[LanguageModel] = selectAllQuery[LanguageModel]

  def insertLanguage(model: LanguageModel): Update0 =
    sql"""
       | INSERT INTO language (name, iso2, iso3, original_name) 
       | VALUES (${model.name}, ${model.iso2}, ${model.iso3}, ${model.originalName}) 
       """.stripMargin.update

  def deleteLanguage(id: Int): Update0 = deleteWhereId[LanguageModel](id)
}
