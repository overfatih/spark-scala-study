package com.profplay.studies
package dataAccess.concretes

import dataAccess.abstracts.{JdbcRepository, UserRepository}


import org.apache.spark.sql.functions.{col, lit}
import org.apache.spark.sql.{DataFrame, SparkSession}

object UserDao extends UserRepository{

  override def userLogin(username: String, password: String, dbTable: DataFrame): DataFrame = {
    val userDF = dbTable
      .filter(col("username") === username && col("password") === password)
    userDF.show()
    userDF
  }

}
