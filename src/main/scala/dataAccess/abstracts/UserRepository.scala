package com.profplay.studies
package dataAccess.abstracts

import com.profplay.studies.core.utilities.results.Result
import org.apache.spark.sql.DataFrame

trait UserRepository {
  def userLogin(username:String, password: String, dbTable:DataFrame) :DataFrame
}
