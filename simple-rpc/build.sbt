name := "SimpleRemote"

version := "1.0"

scalaVersion := "2.10.4"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Akka Maven" at "http://akka.io/repository"

libraryDependencies ++= {
  val akkaVersion  = "2.3.7"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion exclude ("org.scala-lang" , "scala-library"),
    "com.typesafe.akka" %% "akka-remote" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion exclude ("org.slf4j", "slf4j-api")
      exclude ("org.scala-lang" , "scala-library"),
    "org.jlab" %% "common" % "1.0.0"
  )
}
