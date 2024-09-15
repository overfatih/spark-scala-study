package com.profplay.studies
package api.controllers

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.profplay.studies.business.concretes.TableManager.{getTableWithName, join2Table, toDoTaskList}
import com.profplay.studies.entities.concretes.{DataFrameInput, TablesData2, UserData}
import spray.json.DefaultJsonProtocol._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Route
import com.profplay.studies.business.concretes.UserManager
import com.profplay.studies.core.jwt.JwtToken.{createToken, isTokenBlacklisted, logout, secretKey}
import com.profplay.studies.core.utilities.results.DataResult
import com.profplay.studies.dataAccess.concretes.JdbcDao.spark
import org.apache.spark.sql.{DataFrame, Dataset}
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtJson}
import spark.implicits._

import scala.io.StdIn
import scala.util.{Failure, Success}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import play.api.libs.json._


object SimpleApi {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("simple-api-system")
    implicit val materializer: Materializer = Materializer(system)
    implicit val executionContext = system.dispatcher

    implicit val tablesdata = jsonFormat5(TablesData2)
    implicit val userdata = jsonFormat2(UserData)

    import com.profplay.studies.entities.concretes.JsonSupport._

    val route = cors() {
      path("hello") {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to Spark Scala!</h1>"))
        }
      } ~
        path("gettable" / Segment) { dbName =>
          get {
            authorizeRequest { (claim, token) =>
              parameter("tablename") { tableName =>
                complete(StatusCodes.OK, HttpEntity(ContentTypes.`application/json`, (getTableWithName(tableName, dbName).toJSON).collect().mkString("[", ",", "]") + s"[${claim.content}]"))
              }
            }
          }
        } ~
        path("join2table") {
          post {
            authorizeRequest { (claim, token) =>
              entity(as[TablesData2]) { tablesdata =>
                complete(HttpEntity(ContentTypes.`application/json`, (join2Table(tablesdata.df1, tablesdata.cn1, tablesdata.df2, tablesdata.cn2, tablesdata.db).toJSON).collect().mkString("[", ",", "]")))
              }
            }
          }
        } ~
        path(Segment / "login") { memberType =>
          post {
            entity(as[UserData]) { userdata =>
              val result: DataResult[DataFrame] = UserManager.userLogin(userdata.username, userdata.password, memberType, "memberdb")
              if (result.isSuccess) {
                val token = createToken(userdata.username)
                val jsonString: String = result.getData.toJSON.collect().mkString("[", ",", "]")
                val jsonArray: JsArray = Json.parse(jsonString).as[JsArray]
                val additionalData: JsObject = Json.obj(
                  "success" -> JsBoolean(result.isSuccess),
                  "token" -> JsString(s"$token")
                )
                val updatedJsonData: JsObject = additionalData + ("data" -> jsonArray)
                val finalJsonString: String = Json.stringify(updatedJsonData)

                val response: HttpResponse = HttpResponse(
                  entity = HttpEntity(ContentTypes.`application/json`, finalJsonString)
                )
                complete(response)

              } else {
                complete("Invalid credentials")
              }
            }
          }
        } ~
        path(Segment / "logout") { memberType =>
          post {
            authorizeRequest { (claim, token) =>
              logout(token)
              complete(StatusCodes.OK, "Successfully logged out")
            }
          }
        } ~
        path(Segment / "tasklist") { memberType =>
          post {
            entity(as[DataFrameInput]) { input =>
              val jsonStr = input.data
              // JSON'u Spark DataFrame'e dönüştürün
              val taskListdf = spark.read.json(Seq(jsonStr).toDS()) /*neden toDF olmuyorda toDS oluyor*/
              taskListdf.show()
              val result: DataResult[DataFrame] = toDoTaskList(taskListdf)
              if (result.isSuccess) {
                complete(StatusCodes.OK, result.getData.collect().mkString("data[", ",", "]"))
              } else {
                complete(StatusCodes.BadRequest, result.getMessage)
              }


            }
          }
        }
    }


    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")

    StdIn.readLine() // Sunucuyu çalışır durumda tutmak için
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }

  def validateToken(token: String): Either[String, JwtClaim] = {
    JwtJson.decode(token, secretKey, Seq(JwtAlgorithm.HS256)) match {
      case Success(claim) if !isTokenBlacklisted(token) =>
        Right(claim)
      case Success(_) =>
        Left("Token is blacklisted")
      case Failure(ex) =>
        Left(s"Token validation failed: ${ex.getMessage}")
    }
  }

  def authorizeRequest(f: (JwtClaim, String) => Route): Route = {
    optionalHeaderValueByName("Authorization") {
      case Some(token) if token.startsWith("Bearer ") =>
        val actualToken = token.substring(7)
        validateToken(actualToken) match {
          case Right(claim) if !isTokenBlacklisted(actualToken) => f(claim, actualToken)
          case Right(_) => complete(StatusCodes.Unauthorized, "Token is blacklisted")
          case Left(error)  => complete(StatusCodes.Unauthorized, error)
        }
      case _ =>
        complete(StatusCodes.BadRequest, "No valid token provided")
    }
  }




}
