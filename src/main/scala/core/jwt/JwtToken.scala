package com.profplay.studies
package core.jwt

import akka.http.scaladsl.server.Directives.{complete, optionalHeaderValueByName}
import akka.http.scaladsl.server.Route
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim, JwtJson}
import play.api.libs.json.{JsValue, Json}

object JwtToken {
  val secretKey = "my_secret_key"

  def createToken(username: String): String = {
    val claim = Json.obj(
      "username" -> username,
      "expiration" -> (System.currentTimeMillis() / 1000 + 3600) // 1 saat geçerlilik süresi
    )
    JwtJson.encode(claim, secretKey, JwtAlgorithm.HS256)
  }

  def verifyToken(token: String): Option[JsValue] = {
    JwtJson.decodeJson(token, secretKey, Seq(JwtAlgorithm.HS256)).toOption
  }

  def authenticatedRoute(innerRoute: Route): Route = {
    optionalHeaderValueByName("Authorization") {
      case Some(token) if verifyToken(token).isDefined => innerRoute
      case _ => complete("Unauthorized")
    }
  }

  def validateToken(token: String): Either[String, JwtClaim] = {
    JwtJson.decode(token, secretKey, Seq(JwtAlgorithm.HS256)).toEither match {
      case Right(claim) => Right(claim)
      case Left(error) => Left(s"Token validation failed: ${error.getMessage}")
    }
  }

  var tokenBlacklist = Set[String]()

  def logout(token: String): Unit = {
    tokenBlacklist += token
    println(s"Token Blacklist: $tokenBlacklist")
  }

  def isTokenBlacklisted(token: String): Boolean = {
    val result = tokenBlacklist.contains(token)
    println(s"Is token blacklisted? $result")  // Kontrol çıktısını yazdırın
    result
  }
}
