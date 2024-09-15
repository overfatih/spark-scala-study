package com.profplay.studies
package entities.concretes

import spray.json.DefaultJsonProtocol

case class Entity()

case class TablesData2(df1: String, cn1: String, df2: String, cn2: String, db:String)
case class UserData(username: String, password:String)

case class JdbcMetadata(tableName: String, columns: Seq[String])

case class DataFrameInput(data: String)
object JsonSupport extends DefaultJsonProtocol {
  implicit val dataframeInputFormat = jsonFormat1(DataFrameInput)
}

case class selectTableEntity(tableName:String, dbName:String)