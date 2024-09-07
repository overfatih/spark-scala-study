package com.profplay.studies
package business.abstracts

import com.profplay.studies.core.utilities.results.Result
import org.apache.spark.sql.DataFrame

trait UserService {
  def userLogin(userName:String,password:String, memberType:String, dbName:String):Result
  def getTable(tableName:String, dbName:String): DataFrame
}
