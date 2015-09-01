import com.typesafe.sbt.packager.docker._

name := """kontrolletti"""

version := "0.02-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala).enablePlugins(DockerPlugin)

scalaVersion := "2.11.1"

ScoverageSbtPlugin.ScoverageKeys.coverageMinimum := 15
	
ScoverageSbtPlugin.ScoverageKeys.coverageFailOnMinimum := true

ScoverageSbtPlugin.ScoverageKeys.coverageExcludedPackages:= "<empty>;views.*;ReverseAssets.*;ReverseApplication.*;ReverseApiHelpController.*;Routes*;.*routes;Logger.*"

libraryDependencies ++= Seq(
  ws,
  "com.google.inject"  			% "guice" 						% "3.0" 					withSources() withJavadoc(),
  "javax.inject" 				% "javax.inject" 				% "1" 						withSources() withJavadoc(),
  "org.scalatest"				% "scalatest_2.11"  			% "2.2.4"  		% "test" 	withSources() withJavadoc(),
  "org.scalatestplus" 			%% "play" 						% "1.2.0" 		% "test"	withSources() withJavadoc(),
  "org.mockito" 				% "mockito-core" 				% "1.9.5" 		% "test" 	withSources() withJavadoc()
  // Database
  ,"org.postgresql"          	%  "postgresql"  				% "9.4-1202-jdbc41"			withSources() withJavadoc()
  ,"com.typesafe.play" 			%% "play-slick" 				% "0.8.0"					withSources() withJavadoc()
  //,"com.typesafe.play" 			%% "play-slick-evolutions"		% "0.8.0"					withSources() withJavadoc()
  ,"com.typesafe.slick" 		%% "slick" 						% "2.1.x"					withSources() withJavadoc()
  ,"org.slf4j" 					% "slf4j-nop" 					% "1.6.4" 					withSources() withJavadoc()
  ,"com.github.tminglei" 		%% "slick-pg" 					% "0.9.2"					withSources() withJavadoc()
)

// -------------Docker configuration-------------
maintainer in Docker := "fernando.benjamin@zalando.de"

//daemonUser in Docker := "root"
 
// Add this to let Jenkins overwrite your 
dockerRepository :=  Some("pierone.stups.zalan.do/cd") 

dockerBaseImage := "zalando/openjdk:8u40-b09-4"

dockerExposedPorts in Docker := Seq(9000, 9443)

// ------------- Generate scm-source.json ---------
lazy val genScmSource = taskKey[Unit]("Execute the scm-source.sh shell script")

genScmSource := {
  "sh ./scm-source.sh" !
}

mappings in Universal += {
  genScmSource.value
  file( "./scm-source.json") -> "../../scm-source.json" 
}

dockerCommands ++=  Seq(
  Cmd("ADD", "/scm-source.json" + " /scm-source.json")  
)

