package com.profplay.studies
package api.controllers

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.profplay.studies.business.concretes.TableManager.{getTableWithName, join2Table}
import com.profplay.studies.entities.concretes.{TablesData2, UserData}
import spray.json.DefaultJsonProtocol._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.directives.BasicDirectives.extract
import akka.http.scaladsl.server.Route
import com.profplay.studies.business.concretes.UserManager
import com.profplay.studies.core
import com.profplay.studies.core.jwt.JwtToken.{createToken, isTokenBlacklisted, logout, secretKey, tokenBlacklist, validateToken}
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtJson}

import scala.io.StdIn
import scala.util.{Failure, Success}

object SimpleApi {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("simple-api-system")
    implicit val materializer: Materializer = Materializer(system)
    implicit val executionContext = system.dispatcher

    implicit val tablesdata = jsonFormat5(TablesData2)
    implicit val userdata = jsonFormat2(UserData)


    val route =
      path("hello") {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to Spark Scala!</h1>"))
        }
      }~
      path("gettable"/ Segment ) { dbName =>
        get {
          authorizeRequest { (claim, token) =>
            parameter("tablename"){ tableName =>
              complete(StatusCodes.OK, HttpEntity(ContentTypes.`application/json`, (getTableWithName(tableName, dbName).toJSON).collect().mkString("[", ",", "]") + s"[${claim.content}]"))
            }
          }
        }
      }~
      path("join2table") {
        post {
          authorizeRequest { (claim, token) =>
            entity(as[TablesData2]) { tablesdata =>
              complete(HttpEntity(ContentTypes.`application/json`, (join2Table(tablesdata.df1, tablesdata.cn1, tablesdata.df2, tablesdata.cn2, tablesdata.db).toJSON).collect().mkString("[", ",", "]")))
            }
          }
        }
      }~
      path(Segment/"login") { memberType =>
        post {
          entity(as[UserData]) { userdata =>
            if (UserManager.userLogin(userdata.username, userdata.password, memberType, "memberdb").isSuccess) {
              val token = createToken(userdata.username)
              complete(token)
            } else {
              complete("Invalid credentials")
            }
          }
        }
      }~
        path(Segment/"logout") { memberType =>
          post {
            authorizeRequest { (claim, token) =>
              logout(token)
              complete(StatusCodes.OK, "Successfully logged out")
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
