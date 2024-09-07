import scala.collection.Seq

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.14"

lazy val root = (project in file("."))
  .settings(
    name := "sparkapi",
    idePackagePrefix := Some("com.profplay.studies")
  )
val sparkVersion = "3.5.2"
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.postgresql" % "postgresql" % "42.7.4",
  "com.typesafe.akka" %% "akka-http" % "10.5.3",
  "de.heikoseeberger" %% "akka-http-circe" % "1.39.2",
  "com.typesafe.akka" %% "akka-actor" % "2.8.5",
  "com.typesafe.akka" %% "akka-stream" % "2.8.6",
  "com.typesafe" % "config" % "1.4.3",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.5.3",
  "io.spray" %% "spray-json" % "1.3.6",
  "com.pauldijou" %% "jwt-core" % "5.0.0",
  "com.pauldijou" %% "jwt-play-json" % "5.0.0"
)