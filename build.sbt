name := """kontrolleti"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  ws,
  "com.wordnik" %% "swagger-play2" % "1.3.12",
  "com.google.inject"  % "guice"                 % "3.0",
  "javax.inject" % "javax.inject" % "1",
  "org.scalatest"       %   "scalatest_2.11"  % "2.2.4"  % "test",
    "org.specs2"          %%  "specs2-core"     % "2.3.11" % "test",
  "org.mockito" % "mockito-core" % "1.9.5" % "test"
)

// -------------Docker configuration-------------

maintainer in Docker := "fernando.benjamin@zalando.de"


// Add this to let Jenkins overwrite your 
dockerRepository :=  Some("pierone.stups.zalan.do/bus-team") 

dockerBaseImage := "zalando/openjdk:8u40-b09-2"

dockerExposedPorts in Docker := Seq(9000, 9443)