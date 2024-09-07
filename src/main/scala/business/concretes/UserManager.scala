package com.profplay.studies
package business.concretes

import business.abstracts.UserService
import business.abstracts.TableService

import com.profplay.studies.core.utilities.results.{ErrorResult, Result, SuccessResult}
import com.profplay.studies.dataAccess.concretes.{JdbcDao, UserDao}
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.lit


object UserManager extends UserService {

  override def userLogin(userName: String, password: String, memberType:String, dbName:String): Result = {
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
        affectedRows match {
          case rows if rows > 0 => new  SuccessResult("User Login successful")
          case _ => new ErrorResult("Update Error")
        }
      case _ =>
        new ErrorResult("Unknown User")
    }

  }

  override def getTable(tableName: String, dbName:String): DataFrame = {
    TableManager.getTableWithName(tableName,dbName)
  }
  def userUpdateLogedAfterLogin(updatedDF:DataFrame, username: String, password: String, dbName: String, tableName: String): Long = {
    //JdbcDao.updateJdbc(updatedDF,username,password,dbName,tableName) /*dataframe ile tek satır güncelleme başarısız olduğu için alternatif yöntem deneniyor*/
    JdbcDao.updateLogedStatus(username, password, dbName, tableName)
  }

  

  def userLogout(userName: String) = {

  }


}
