package com.profplay.studies
package dataAccess.concretes

import com.profplay.studies.entities.concretes.JdbcMetadata
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._

class GetJbdcMetadata {
  // SparkSession oluşturma
  val spark = SparkSession.builder()
    .appName("PostgreSQL Metadata")
    .master("local[*]")
    .config("spark.driver.bindAddress", "127.0.0.1")
    .getOrCreate()

  def getAllDatameta(dbName: String, customerUserName: String, customerPassword: String): DataFrame = {

    // PostgreSQL'e bağlanma
    val jdbcUrl = s"jdbc:postgresql://localhost:5432/$dbName"
    val connectionProperties = new java.util.Properties()
    connectionProperties.setProperty("user", customerUserName)
    connectionProperties.setProperty("password", customerPassword)

    // Tabloları ve sütunları sorgulama
    val tablesDF = spark.read.jdbc(jdbcUrl, "information_schema.tables", connectionProperties).filter("table_schema = 'public'")
    val columnsDF = spark.read.jdbc(jdbcUrl, "information_schema.columns", connectionProperties)

    // Tabloları ve sütunları birleştirme
    val tableColumnsDF = tablesDF.join(columnsDF, "table_name")
      .groupBy("table_name")
      .agg(collect_list("column_name").alias("tableColumns"))

    // Sonuç DataFrame'i gösterme
    //val rowCount = tableColumnsDF.count()
    //println(s"Total number of rows: $rowCount")
    tableColumnsDF.sort(col("table_name").asc)/*.show(Int.MaxValue, false)*/
    tableColumnsDF
  }

}

object GetJbdcMetadata {
  def apply(dbName:String, customerUserName:String, customerPassword:String): GetJbdcMetadata = new GetJbdcMetadata(){
    getAllDatameta(dbName,customerUserName,customerPassword)
  }
  def apply(): GetJbdcMetadata = {new GetJbdcMetadata()}

}





