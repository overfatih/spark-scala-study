package com.profplay.studies
package dataAccess.concretes

import org.apache.spark.sql.{DataFrame, SparkSession}
import com.profplay.studies.dataAccess.abstracts.JdbcRepository
import org.apache.spark.sql.functions.col

object JdbcDao extends JdbcRepository{
  val spark: SparkSession = SparkSession.builder()
    .appName("sparkapi")
    .master("local[*]")
    .config("spark.driver.bindAddress", "127.0.0.1")
    .getOrCreate()

  spark.sparkContext.setLogLevel("ERROR")

  override def getTable(name: String, dbName:String): DataFrame = {
    getJdbc(spark,"postgres","123456", dbName, name)
  }

  override def getJdbc(spark:SparkSession, user: String, password: String, dbName: String, tableName: String): DataFrame = {
    val jdbcDF = spark.read
      .format("jdbc")
      .option("url", s"jdbc:postgresql://localhost:5432/$dbName")
      .option("dbtable", s"\"$tableName\"")
      .option("user",user)
      .option("password",password)
      .load()
    jdbcDF
  }

  override def updateJdbc(jdbcDF:DataFrame, user: String, password: String, dbName: String, tableName: String): Long = {
    jdbcDF.write
      .format("jdbc")
      .option("url", s"jdbc:postgresql://localhost:5432/$dbName")
      .option("dbtable", s"\"$tableName\"")
      .option("user",user)
      .option("password",password)
      .mode("overwrite")
      .save()
    jdbcDF.show()
    jdbcDF.count()
  }

  override def join2Table(df1: DataFrame, columnName1: String, df2: DataFrame, columnName2: String): DataFrame = {
    val joinedDF = df1.join(df2,df1(columnName1) === df2(columnName2))
    joinedDF
  }


  import java.sql.Connection
  import java.sql.DriverManager
  import java.sql.PreparedStatement

  // Veritabanı bağlantısı için fonksiyon
  override def getConnection(dbName: String): Connection = {
    val url = s"jdbc:postgresql://localhost:5432/$dbName"
    val user = "postgres"
    val password = "123456"
    DriverManager.getConnection(url, user, password)
  }

  // Güncelleme işlemini gerçekleştirmek için fonksiyon
  override def updateLogedStatus(username: String, password: String, dbName:String, tableName:String): Int = {
    val conn = getConnection(dbName)
    val updateSQL = s"UPDATE $tableName SET loged = true WHERE username = ? AND password = ?"
    val statement: PreparedStatement = conn.prepareStatement(updateSQL)

    try {
      statement.setString(1, username)
      statement.setString(2, password)

      // Güncellenen satır sayısını döner
      statement.executeUpdate()
    } finally {
      statement.close()
      conn.close()
    }
  }
}
