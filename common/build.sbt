import sbt._
//import Keys._

organization := "org.jlab"

name := "common"

version := "1.0"

scalaVersion := "2.10.4"

//resolvers += "spray repo" at "http://repo.spray.io"

libraryDependencies ++= {
  val akkaVersion  = "2.3.7"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion exclude ("org.scala-lang" , "scala-library"),
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion exclude ("org.slf4j", "slf4j-api")
                                                      exclude ("org.scala-lang" , "scala-library"),
    "com.typesafe.akka" %% "akka-remote" % akkaVersion,
    "org.json4s"        % "json4s-native_2.10"   % "3.2.11",
    "com.typesafe.akka" %% "akka-testkit"     % akkaVersion   % "test",
    "org.scalatest"     %   "scalatest_2.10"  % "2.0" % "test"
  )
}

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-dead-code",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8"
)

//testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")

crossPaths := false
