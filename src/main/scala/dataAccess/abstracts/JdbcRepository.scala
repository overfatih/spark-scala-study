package com.profplay.studies
package dataAccess.abstracts

import com.profplay.studies.core.utilities.results.DataResult
import org.apache.spark.sql.{DataFrame, DataFrameReader, SparkSession}

import java.sql.Connection



trait JdbcRepository {
  def getJdbc(spark:SparkSession, user: String, password:String, dbName:String, tableName:String): DataFrame
  def updateJdbc(dataFrame:DataFrame, user: String, password: String, dbName: String, tableName: String): Long
  def getTable(name: String, dbName: String): DataFrame
  def join2Table(df1:DataFrame, columnName1:String,df2:DataFrame,columnName2:String): DataFrame

  def getConnection(dbName: String): Connection
  def updateLogedStatus(username: String, password: String, dbName:String, tableName:String): Int
  def getAction(dataFrameReader: DataFrameReader): DataResult[DataFrame]
  def filterTableDao(dataFrameBase:DataFrame, columnName:String, value:String):DataFrame
}

