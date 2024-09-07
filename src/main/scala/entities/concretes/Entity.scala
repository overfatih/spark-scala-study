package com.profplay.studies
package entities.concretes

import org.apache.spark.sql.DataFrame

case class Entity()

case class TablesData2(df1: String, cn1: String, df2: String, cn2: String, db:String)
case class UserData(username: String, password:String)
