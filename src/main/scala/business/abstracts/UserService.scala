package com.profplay.studies
package business.abstracts

import com.profplay.studies.core.utilities.results.{DataResult, Result}
import org.apache.spark.sql.DataFrame

trait UserService {
  def userLogin(userName:String,password:String, memberType:String, dbName:String):DataResult[DataFrame]
  def getTable(tableName:String, dbName:String): DataFrame
  def filterTable(columnName:String, value:String, tableName: String, dbName:String):DataFrame

}
