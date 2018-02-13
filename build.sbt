name := "http-client"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.0.11",
  "io.spray" %% "spray-json" % "1.3.3",
  "com.typesafe" % "config" % "1.3.1",
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.11",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)
