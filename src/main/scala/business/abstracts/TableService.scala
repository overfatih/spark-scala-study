package com.profplay.studies
package business.abstracts


import com.profplay.studies.core.utilities.results.DataResult
import org.apache.spark.sql.{DataFrame, DataFrameReader}


trait TableService {
  def getTableWithName(tableName:String, dbName:String):DataFrame
  def join2Table(df1Name:String, df1columnName:String,df2Name:String,df2columnName:String, dbName:String):DataFrame
  def toDoTaskList(taskDf:DataFrame):DataResult[DataFrame]
}
