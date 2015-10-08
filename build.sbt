import com.typesafe.sbt.packager.docker._

name := """kontrolletti"""

version := "0.02-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala).enablePlugins(DockerPlugin)

scalaVersion := "2.11.1"

/*scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-Xlint", // Enable recommended additional warnings.
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
  "-Ywarn-nullary-override", // Warn when non-nullary overrides nullary, e.g. def foo() over def foo.
  "-Ywarn-numeric-widen" // Warn when numerics are widened.
)*/

ScoverageSbtPlugin.ScoverageKeys.coverageMinimum := 15
	
ScoverageSbtPlugin.ScoverageKeys.coverageFailOnMinimum := true

ScoverageSbtPlugin.ScoverageKeys.coverageExcludedPackages:= "<empty>;views.*;ReverseAssets.*;ReverseApplication.*;ReverseApiHelpController.*;Routes*;.*routes;Logger.*"

libraryDependencies ++= {
	val playVersion = "2.4.2"
	Seq(
		ws,
		"com.google.inject"  		% "guice" 						% "3.0" 					withSources() withJavadoc(),
		"javax.inject" 				% "javax.inject" 				% "1" 						withSources() withJavadoc(),
		"org.scalatest"				% "scalatest_2.11"  			% "2.2.5"  		% "test" 	withSources() withJavadoc(),
		"org.scalatestplus" 		%% "play" 						% "1.4.0-M3" 	% "test"	withSources() withJavadoc(),
		"org.mockito" 				% "mockito-core" 				% "1.9.5" 		% "test" 	withSources() withJavadoc()
	
		// Database
		,"com.h2database" 			% "h2" 							% "1.4.188"		% "test"   	withSources() withJavadoc()
//		,"com.typesafe.play" 		% "play-jdbc_2.11" 				% playVersion	% "test"   	withSources() withJavadoc()
		,"com.typesafe.play"		% "play-test_2.11" 				% playVersion	% "test"   	withSources() withJavadoc()
		,"org.postgresql"          	%  "postgresql"  				% "9.4-1201-jdbc41"			withSources() withJavadoc()
		,"com.typesafe.play" 		%% "play-slick" 				% "1.0.1"					withSources() withJavadoc()
		,"com.typesafe.play" 		%% "play-slick-evolutions"		% "1.0.1"					withSources() withJavadoc()
		// Dependencies of slick-pg
		,"com.typesafe.slick" 		%% "slick-codegen" 				% "3.0.1+"					withSources() withJavadoc()
		,"com.github.tminglei" 		%% "slick-pg" 					% "0.9.1"					withSources() withJavadoc()
		,"com.vividsolutions" 		% "jts" 						% "1.13" 					withSources() withJavadoc()
		// Joda
		,"joda-time" 				% "joda-time" 					% "2.7"					withSources() withJavadoc()
  		,"org.joda" 				% "joda-convert" 				% "1.7"					withSources() withJavadoc()
	  
	)
}

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

