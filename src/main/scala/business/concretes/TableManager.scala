package com.profplay.studies
package business.concretes

import business.abstracts.TableService

import com.profplay.studies.dataAccess.abstracts.JdbcRepository
import com.profplay.studies.dataAccess.concretes.JdbcDao

import org.apache.spark.sql.DataFrame

object TableManager extends TableService{

  override def getTableWithName(tableName: String, dbName: String): DataFrame = {
    JdbcDao.getTable(tableName, dbName)
  }

  override def join2Table(df1Name: String, df1columnName: String, df2Name: String, df2columnName: String, dbName:String): DataFrame = {
    JdbcDao.join2Table(JdbcDao.getTable(df1Name,dbName), df1columnName, JdbcDao.getTable(df2Name,dbName), df2columnName)
  }
}
