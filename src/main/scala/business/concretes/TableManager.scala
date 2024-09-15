package com.profplay.studies
package business.concretes

import business.abstracts.TableService


import com.profplay.studies.core.utilities.results.{DataResult, ErrorDataResult, SuccessDataResult}
import com.profplay.studies.dataAccess.abstracts.JdbcRepository
import com.profplay.studies.dataAccess.concretes.JdbcDao
import com.profplay.studies.dataAccess.concretes.JdbcDao.getJdbc
import com.profplay.studies.entities.concretes.selectTableEntity
import org.apache.spark.sql.{DataFrame, DataFrameReader, Row}



object TableManager extends TableService{

  override def getTableWithName(tableName: String, dbName: String): DataFrame = {
    JdbcDao.getTable(tableName, dbName)
  }

  override def join2Table(df1Name: String, df1columnName: String, df2Name: String, df2columnName: String, dbName:String): DataFrame = {
    JdbcDao.join2Table(JdbcDao.getTable(df1Name,dbName), df1columnName, JdbcDao.getTable(df2Name,dbName), df2columnName)
  }

  override def toDoTaskList(taskDf: DataFrame): DataResult[DataFrame] = {
    var dataFrames: Seq[DataFrame] = Seq.empty[DataFrame]
    /*task = {nodeType:"", nodeInput:"", nodeOutput:"", taskBody:}*/
    taskDf.foreach( task =>
      task(0) match {
        case "select" =>
          val selectTableE = task(3) match {
            case entity: selectTableEntity => entity // Eğer task(3) zaten selectTableEntity ise, direkt olarak kullanılır
            case _ => throw new RuntimeException(s"task(3) is not of type selectTableEntity, but found ${task(3).getClass.getSimpleName}")
          }
          dataFrames = dataFrames :+ getTableWithName(selectTableE.tableName, selectTableE.dbName)
        /*case "filter" => "goto filter function"
        case "join" => "goto join function"
        case "action" => "goto action function"
        case _ => "Something went wrong"*/
        }

        /*bu arada case'ten gelen sonucu alıp tekrar işleme sokmak lazım (?)*/
        /*
        transformationsFunction={join,filter,sort,groupBy,select,where,withColumn}
        DataFrameNaFunctions    = "https://spark.apache.org/docs/3.5.2/api/scala/org/apache/spark/sql/DataFrameNaFunctions.html"
        DataFrameReader         = "https://spark.apache.org/docs/3.5.2/api/scala/org/apache/spark/sql/DataFrameReader.html"
        DataFrameStatFunctions  = "https://spark.apache.org/docs/3.5.2/api/scala/org/apache/spark/sql/DataFrameStatFunctions.html"
        DataFrameWriter         = "https://spark.apache.org/docs/3.5.2/api/scala/org/apache/spark/sql/DataFrameWriter.html"
        DataFrameWriterV2       = "https://spark.apache.org/docs/3.5.2/api/scala/org/apache/spark/sql/DataFrameWriterV2.html"
        def toDoTaskWithFilter(nodeInput,nodeBody):nodeOutputString{}
        def toDoTaskWithJoin(nodeInput,nodeBody):nodeOutputString{}
        def toDoTaskWithAction(dataFrame: DataFrame):DataFrame{actionsFunction= {load(),save()}}*/
    )
  new DataResult[DataFrame](dataFrames(1),true)
  }

}
