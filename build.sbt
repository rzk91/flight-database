scalaVersion := "2.13.8"
name         := "flight-database"
organization := "rzk.scala"
version      := "1.0"

libraryDependencies ++= circeDependencies ++ otherDependencies

val circeVersion = "0.14.1"

val circeDependencies = Seq(
  "io.circe" %% "circe-core"           % circeVersion,
  "io.circe" %% "circe-generic"        % circeVersion,
  "io.circe" %% "circe-parser"         % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion
)

val otherDependencies = Seq(
  "com.github.pureconfig"      %% "pureconfig"    % "0.17.2",
  "org.slf4j"                  % "slf4j-log4j12"  % "1.7.36",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
  "commons-io"                 % "commons-io"     % "2.11.0",
  "org.postgresql"             % "postgresql"     % "42.2.5" % "provided",
  "org.flywaydb"               % "flyway-core"    % "7.0.4"
)

scalacOptions ++= Seq(
  "-encoding",
  "utf8",
  "-Xfatal-warnings",
  "-deprecation",
  "-unchecked",
  "-explaintypes",
  "-Ymacro-annotations",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps"
)
