package com.profplay.studies
package business.concretes

import business.abstracts.UserService

import com.profplay.studies.core.utilities.results.{DataResult, ErrorDataResult, SuccessDataResult}
import com.profplay.studies.dataAccess.concretes.JdbcDao.filterTableDao
import com.profplay.studies.dataAccess.concretes.{GetJbdcMetadata, JdbcDao, UserDao}
import org.apache.spark.sql.{DataFrame, DataFrameReader}
import org.apache.spark.sql.functions.{col, lit}


object UserManager extends UserService {

  override def userLogin(userName: String, password: String, memberType:String, dbName:String): DataResult[DataFrame] = {
    val dbTable:DataFrame = getTable(memberType,dbName)
    val userDF: DataFrame = UserDao.userLogin(userName, password, dbTable)
    val newRowsCount = userDF.count()
    newRowsCount match {
      case newRows if newRows > 0 =>
        // `loged` sütununu güncellemek
        val updatedDF = userDF.withColumn("loged", lit(true))
        println(f"--*10 updated dataFrame --*10")
        updatedDF.show()
        val affectedRows = userUpdateLogedAfterLogin(updatedDF,userName, password, dbName, memberType)
        val userId = userDF.select("id").first().getLong(0)
        println(userId)
        val userDbInfo: DataFrame = filterTable("userid", userId.toString, "customerdbinfo", "memberdb")

        affectedRows match {
          case rows if rows > 0 =>
            val getMetadata = getJbdcMetadata(userDbInfo.select("dbname").first().getString(0),userDbInfo.select("dbusername").first().getString(0), userDbInfo.select("dbpassword").first().getString(0))
            new SuccessDataResult[DataFrame](getMetadata,"User Login successful")
          case _ => new ErrorDataResult[DataFrame]("Update Error")
        }
      case _ =>
        new ErrorDataResult[DataFrame]("Unknown User")
    }

  }


  override def getTable(tableName: String, dbName:String): DataFrame = {
    TableManager.getTableWithName(tableName,dbName)
  }
  def userUpdateLogedAfterLogin(updatedDF:DataFrame, username: String, password: String, dbName: String, tableName: String): Long = {
    //JdbcDao.updateJdbc(updatedDF,username,password,dbName,tableName) /*dataframe ile tek satır güncelleme başarısız olduğu için alternatif yöntem deneniyor*/
    JdbcDao.updateLogedStatus(username, password, dbName, tableName)
  }

  

  def getJbdcMetadata(dbName: String, customerUserName: String, customerPassword: String):DataFrame = {
    val getMetadata = GetJbdcMetadata()
    val df1 = getMetadata.getAllDatameta(dbName,customerUserName,customerPassword)
    df1
  }


  override def filterTable(columnName: String, value: String, tableName: String, dbName:String): DataFrame = {
    filterTableDao(getTable(tableName, dbName), columnName,value)
  }
}
